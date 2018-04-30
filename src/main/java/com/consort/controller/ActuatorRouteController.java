package com.consort.controller;

import com.consort.database.entities.Status;
import com.consort.service.ActuatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import spark.Service;

import static spark.Service.ignite;

public class ActuatorRouteController implements RouteController {

    public void initRoutes() {
        final Service http = ignite().port(8081);
        http.get("/health", (req, res) -> {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(new Status("UP"));
        });
        http.get("/metrics", (req, res) -> {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(ActuatorService.getInstance().getCounters(res));
        });
        http.get("/metrics/:name", (req, res) -> {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(ActuatorService.getInstance().getCounterByName(req.params("name")));
        });
    }
}
