package conduit.endpoints

import conduit.Router
import io.mockk.mockk

fun getRouterToTest() = Router(
    loginHandler = mockk(relaxed = true),
    registerUserHandler = mockk(relaxed = true)
)