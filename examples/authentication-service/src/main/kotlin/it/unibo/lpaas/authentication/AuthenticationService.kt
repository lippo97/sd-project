@file:Suppress("MatchingDeclarationName")
package it.unibo.lpaas.authentication

import com.fasterxml.jackson.databind.module.SimpleModule
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import io.vertx.core.Vertx
import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.authentication.provider.Credentials
import it.unibo.lpaas.authentication.provider.CredentialsProvider
import it.unibo.lpaas.authentication.provider.Password
import it.unibo.lpaas.authentication.provider.UserDTO
import it.unibo.lpaas.authentication.provider.Username
import it.unibo.lpaas.authentication.serialization.PasswordDeserializer
import it.unibo.lpaas.authentication.serialization.RoleDeserializer
import it.unibo.lpaas.authentication.serialization.UsernameDeserializer
import it.unibo.lpaas.environment.Environment
import it.unibo.lpaas.http.databind.SerializerConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.div
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.util.KMongoConfiguration

fun main() {
    SerializerConfiguration.defaultWithModule {
        addDeserializer(Username::class.java, UsernameDeserializer())
        addDeserializer(Password::class.java, PasswordDeserializer())
    }.applyOnJackson()

    KMongoConfiguration.registerBsonModule(
        SimpleModule().apply {
            addDeserializer(Role::class.java, RoleDeserializer())
        }
    )

    val vertx = Vertx.vertx()

    val mongoClientSettings = MongoClientSettings.builder()
        .applyConnectionString(ConnectionString(Environment.getString("MONGO_CONNECTION_STRING")))
        .build()
    val client = KMongo.createClient(mongoClientSettings).coroutine
    val database = client.getDatabase(Environment.getString("MONGO_DATABASE"))

    val port = Environment.getInt("PORT")
    val jwtProvider = JWTAuthFactory.hs256SecretBased(vertx, Environment.getString("JWT_SECRET"))
    val userCollection = database.getCollection<UserDTO>("user").apply {
        GlobalScope.launch(Dispatchers.IO) {
            createIndex(
                Indexes
                    .ascending((UserDTO::credentials / Credentials::username).name),
                IndexOptions()
                    .unique(true)
            )
        }
    }
    val credentialsProvider = CredentialsProvider.mongo(vertx, userCollection)

    vertx.createHttpServer()
        .requestHandler(AuthController.make(vertx, jwtProvider, credentialsProvider).routes())
        .listen(port)
        .onSuccess {
            println("Running...")
        }
}
