// Create Services
CREATE 
(payment:Service {
    name: "Payment Service",
    description: "Handles customer payment processing"
}),
(customer:Service {
    name: "Customer Service",
    description: "Manages customer profiles"
}),
(auth:Service {
    name: "Authentication Service",
    description: "Handles user authentication"
});


// Create Teams
CREATE
(finance:Team {
    name: "Finance Team"
}),
(platform:Team {
    name: "Platform Engineering"
});


// Create Databases
CREATE
(paymentDb:Database {
    name: "Payment Database"
}),
(customerDb:Database {
    name: "Customer Database"
});


// Create relationships

CREATE
(payment)-[:OWNED_BY]->(finance),
(customer)-[:OWNED_BY]->(platform),

(payment)-[:USES]->(paymentDb),
(customer)-[:USES]->(customerDb),

(payment)-[:DEPENDS_ON]->(auth),
(customer)-[:DEPENDS_ON]->(auth);