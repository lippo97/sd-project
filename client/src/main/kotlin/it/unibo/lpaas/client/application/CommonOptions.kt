package it.unibo.lpaas.client.application

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int

class CommonOptions : OptionGroup() {
    val hostname: String by option(help = "Name of the remote LpaaS host").required()
    @Suppress("MagicNumber")
    val port: Int by option(help = "Port used by the remote LpaaS host").int().required()
        .check("Must be between 1 and 65535") {
            it in 1..(65535)
        }
    val username: String by option(help = "Username used to authenticate").required()
    val password: String by option(help = "Password used to authenticate").required()
    val verbose: Boolean by option("-v", "--verbose").flag()
}
