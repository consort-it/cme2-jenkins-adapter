package com.consort;

import com.consort.controller.ActuatorRouteController;
import com.consort.controller.RouteController;
import com.consort.controller.JenkinsAdapterRouteController;

import java.util.HashSet;
import java.util.Set;

import static spark.Spark.internalServerError;
import static spark.Spark.notFound;

public class JenkinsAdapterMain {

    private static Set<RouteController> routeControllers = new HashSet<>();

    public static void main(String[] args) {

        registerRouteControllers();

        initRoutes();

        notFound((req, res) -> {
            res.type("application/json");
            return "{\"message\":\"Mapping not found.\", \"code\":\"JENK-404\"}";
        });

        internalServerError((req, res) -> {
            res.type("application/json");
            return "{\"message\":\"Internal server error\", \"code\":\"JENK-500\"}";
        });
    }

    private static void registerRouteControllers() {
        routeControllers.add(new JenkinsAdapterRouteController());
        routeControllers.add(new ActuatorRouteController());
    }

    private static void initRoutes() {
        for(final RouteController routeController : routeControllers) {
            routeController.initRoutes();
        }
    }
}
