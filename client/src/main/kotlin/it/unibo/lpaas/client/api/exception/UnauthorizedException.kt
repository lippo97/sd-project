package it.unibo.lpaas.client.api.exception

import io.netty.handler.codec.http.HttpResponseStatus

class UnauthorizedException : HTTPException(HttpResponseStatus.UNAUTHORIZED.code())
