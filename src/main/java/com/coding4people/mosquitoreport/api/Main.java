package com.coding4people.mosquitoreport.api;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import com.coding4people.mosquitoreport.api.buckets.BucketBinder;
import com.coding4people.mosquitoreport.api.controllers.AuthEmailController;
import com.coding4people.mosquitoreport.api.controllers.FocusController;
import com.coding4people.mosquitoreport.api.controllers.PictureController;
import com.coding4people.mosquitoreport.api.controllers.SignUpController;
import com.coding4people.mosquitoreport.api.exceptionmapper.BadRequestExceptionMapper;
import com.coding4people.mosquitoreport.api.exceptionmapper.ForbiddenExceptionMapper;
import com.coding4people.mosquitoreport.api.exceptionmapper.InternalServerErrorExceptionMapper;
import com.coding4people.mosquitoreport.api.exceptionmapper.NotFoundExceptionMapper;
import com.coding4people.mosquitoreport.api.factories.FactoryBinder;
import com.coding4people.mosquitoreport.api.indexers.IndexerBinder;
import com.coding4people.mosquitoreport.api.repositories.RepositoryBinder;

public class Main {
    public static final String BASE_URI = "http://0.0.0.0:9000/";

    public static ResourceConfig createApp() {
        return commonConfig()
                // Controllers
                .register(AuthEmailController.class)
                .register(FocusController.class)
                .register(PictureController.class)
                .register(SignUpController.class)
                
                // Exception mappers
                .register(BadRequestExceptionMapper.class)
                .register(ForbiddenExceptionMapper.class)
                .register(InternalServerErrorExceptionMapper.class)
                .register(NotFoundExceptionMapper.class)
                
                // Binders
                .register(new BucketBinder())
                .register(new FactoryBinder())
                .register(new IndexerBinder())
                .register(new RepositoryBinder());
    }
    
    public static ResourceConfig commonConfig() {
        return new ResourceConfig().property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true)
                .property(CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true)
                .register(JacksonFeature.class)
                .register(MultiPartFeature.class);
    }

    public static void main(String[] args) throws IOException {
        try {
            final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), createApp());

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    server.shutdownNow();
                }
            }));

            server.start();

            Thread.currentThread().join();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
