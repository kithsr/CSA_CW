package com.smartcampus;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS application entry point for the Smart Campus API.
 * The @ApplicationPath annotation establishes the versioned base URI for all resources.
 * All resource classes under the com.smartcampus package are auto-discovered by Jersey.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {
}
