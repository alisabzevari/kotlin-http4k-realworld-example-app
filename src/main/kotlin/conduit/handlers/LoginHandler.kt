package conduit.handlers

import conduit.model.*

class LoginHandler {
    fun handle(loginInfo: LoginInfo) =
        LoggedinUserInfo(
            loginInfo.email,
            Token(loginInfo.password.value),
            Username(loginInfo.email.value),
            Bio("bio-1"),
            Image("image-1")
        )
}