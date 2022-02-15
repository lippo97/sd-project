package it.unibo.lpaas

class EnvironmentVariableException(val name: String) :
    Throwable(message = "Couldn't find the requested environment variable $name")
