package org.activiti.cloud.services.graphql.graphiql;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GraphiQLAutoConfigurationTest {
    
    @Autowired
    private KeycloakJsonController keycloakJsonController;

    @SpringBootApplication
    static class Application {
        // 
    }
    
    @Test
    public void contextLoads() {
        assertThat(keycloakJsonController).isNotNull();
    }

}
