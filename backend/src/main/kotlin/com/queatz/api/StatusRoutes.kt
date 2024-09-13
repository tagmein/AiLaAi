package com.queatz.api

import com.queatz.db.Status
import com.queatz.db.statuses
import com.queatz.notBlank
import com.queatz.plugins.db
import com.queatz.plugins.me
import com.queatz.plugins.respond
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.statusRoutes() {
    authenticate {
        get("/statuses") {
            respond {
                db.statuses(me.id!!)
            }
        }
        post("/statuses") {
            respond {
                val newStatus = call.receive<Status>()
                db.insert(
                    Status(
                        name = newStatus.name?.trim()?.notBlank,
                        color = newStatus.color?.trim()
                    )
                )
            }
        }
    }
}
