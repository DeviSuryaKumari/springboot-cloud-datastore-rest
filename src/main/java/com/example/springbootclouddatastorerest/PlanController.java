package com.example.springbootclouddatastorerest;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.common.collect.Lists;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.google.cloud.datastore.*;

import java.io.IOException;
import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/cs218spring2023/rest")
public class PlanController {

    final String DEFAULT_HEADER_NAME = "CS218SPRING2023-WebSvc", DEFAULT_HEADER_VALUE = "Devi Surya Kumari 036";
    final String DATASTORE_NAMESPACE = "cs218restapi";
    final String KIND = "Plan";
    Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private KeyFactory keyFactory = datastore.newKeyFactory().setKind("Plan").setNamespace(DATASTORE_NAMESPACE);

    public PlanController() throws IOException {
    }

    // get all plans rest API
    @GetMapping("/plan")
    public ResponseEntity<List<Plan>> getAllPlans(@RequestParam Optional<String> sortby) {

        Query<Entity> query = sortby.isPresent() ? Query.newEntityQueryBuilder().setKind(KIND).setNamespace(DATASTORE_NAMESPACE).setOrderBy(StructuredQuery.OrderBy.asc(sortby.get())).build()
                : Query.newEntityQueryBuilder().setKind(KIND).setNamespace(DATASTORE_NAMESPACE).build();
        QueryResults<Entity> entities = datastore.run(query);

        List<Plan> plans = new ArrayList<>();
        if (entities.hasNext()) {
            while (entities.hasNext()) {
                Entity entity = entities.next();

                Long id = entity.getKey().getId();
                String title = entity.getString("title");
                Double budget = entity.getDouble("budget");
                Plan plan = new Plan(id, title, budget);
                plans.add(plan);
            }
            return ResponseEntity.ok().header(DEFAULT_HEADER_NAME, DEFAULT_HEADER_VALUE).body(Lists.newArrayList(plans));
        }
        return ResponseEntity.notFound().build();
    }

    // get plan by id rest api
    @GetMapping("/plan/{id}")
    public ResponseEntity<Plan> getPlanById(@PathVariable Long id) {
        Query<Entity> query =
                Query.newEntityQueryBuilder()
                        .setKind(KIND)
                        .setNamespace(DATASTORE_NAMESPACE)
                        .setFilter(StructuredQuery.PropertyFilter.eq("__key__", keyFactory.newKey(id)))
                        .build();
        QueryResults<Entity> entities = datastore.run(query);
        if (entities.hasNext()) {
            Entity entity = entities.next();
            String title = entity.getString("title");
            Double budget = entity.getDouble("budget");
            Plan plan = new Plan(id, title, budget);
            return ResponseEntity.ok().header(DEFAULT_HEADER_NAME, DEFAULT_HEADER_VALUE).body(plan);
        }
        return ResponseEntity.notFound().build();
    }

    // create plan rest API
    @PostMapping(path="/plan")
    public ResponseEntity<?> createPlan(@RequestBody Plan plan) {
        try {
            Key taskKey = datastore.allocateId(keyFactory.newKey());
            Entity savedPlanEntity =
                    Entity.newBuilder(taskKey)
                            .set("title", plan.getTitle())
                            .set("budget", plan.getBudget())
                            .build();
            datastore.add(savedPlanEntity);
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(taskKey.getId())
                    .toUri();
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set(DEFAULT_HEADER_NAME, DEFAULT_HEADER_VALUE);
            responseHeaders.set(HttpHeaders.LOCATION, String.valueOf(location));

            Plan savedPlan = new Plan(taskKey.getId(), plan.getTitle(), plan.getBudget());
            return ResponseEntity.created(location).headers(responseHeaders).body(savedPlan);
        } catch (DatastoreException datastoreException) {
            return ResponseEntity.status(500).build();
        }
    }

    // update plan rest api
    @PutMapping("/plan/{id}")
    public ResponseEntity<Plan> updatePlan(@PathVariable Long id, @RequestBody Plan planDetails) {
        Query<Entity> query =
                Query.newEntityQueryBuilder()
                        .setKind(KIND)
                        .setNamespace(DATASTORE_NAMESPACE)
                        .setFilter(StructuredQuery.PropertyFilter.eq("__key__", keyFactory.newKey(id)))
                        .build();
        QueryResults<Entity> entities = datastore.run(query);
        if (entities.hasNext()) {
            Entity entity = entities.next();
            String title = entity.getString("title");
            Double budget = entity.getDouble("budget");
            Plan plan = new Plan(id, title, budget);

            if (planDetails.getTitle() != null) {
                plan.setTitle(planDetails.getTitle());
            }
            if (planDetails.getBudget() != null) {
                plan.setBudget(planDetails.getBudget());
            }
            Entity updatedPlanEntity = Entity.newBuilder(datastore.get(keyFactory.newKey(id))).set("title", plan.getTitle()).set("budget", plan.getBudget()).build();
            datastore.update(updatedPlanEntity);
            return ResponseEntity.ok().header(DEFAULT_HEADER_NAME, DEFAULT_HEADER_VALUE).body(plan);
        }
        return ResponseEntity.notFound().build();
    }

    // delete plan rest api
    @DeleteMapping("/plan/{id}")
    public ResponseEntity<?> deletePlan (@PathVariable Long id) {
        Query<Entity> query =
                Query.newEntityQueryBuilder()
                        .setKind(KIND)
                        .setNamespace(DATASTORE_NAMESPACE)
                        .setFilter(StructuredQuery.PropertyFilter.eq("__key__", keyFactory.newKey(id)))
                        .build();
        QueryResults<Entity> entities = datastore.run(query);
        if (entities.hasNext()) {
            datastore.delete(keyFactory.newKey(id));
            Map<String, Boolean> response = new HashMap<>();
            response.put("deleted", Boolean.TRUE);
            return ResponseEntity.ok().header(DEFAULT_HEADER_NAME, DEFAULT_HEADER_VALUE).body(response);
        }
        return ResponseEntity.notFound().build();
    }
}
