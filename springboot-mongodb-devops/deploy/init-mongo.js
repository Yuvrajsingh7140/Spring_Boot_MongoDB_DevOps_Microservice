// Initialize MongoDB database
db = db.getSiblingDB('devops_db');

// Create collections
db.createCollection('users');

// Create indexes
db.users.createIndex({ "username": 1 }, { unique: true });
db.users.createIndex({ "email": 1 }, { unique: true });
db.users.createIndex({ "createdAt": 1 });
db.users.createIndex({ "active": 1 });

// Insert sample data
db.users.insertMany([
  {
    username: "admin",
    email: "admin@company.com",
    password: "$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRdvdHxZ3rJim/.f.6eM8LkSGbu", // password123
    firstName: "System",
    lastName: "Administrator",
    active: true,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    username: "testuser",
    email: "test@company.com", 
    password: "$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRdvdHxZ3rJim/.f.6eM8LkSGbu", // password123
    firstName: "Test",
    lastName: "User",
    active: true,
    createdAt: new Date(),
    updatedAt: new Date()
  }
]);

print("Database initialization completed!");