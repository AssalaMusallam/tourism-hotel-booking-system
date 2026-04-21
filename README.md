# Tourism Hotel Booking System

**Course:** SWER313 — Step 1  
**Group:** PinkFlow  
**Team:** Mayar, Assala, Mais

A modular Spring Boot monolith backend for a tourism hotel booking platform. The system allows guests to browse hotels, check availability, create bookings, make mock payments, and receive notifications. Managers and administrators can manage hotels, room types, pricing rules, and reports.

---

## Project Overview

This project was developed for **Step 1** of the SWER313 course project.  
The system is implemented as a **modular monolith** using Spring Boot, with clear separation between the main business modules:

- `catalog`
- `availabilitypricing`
- `booking`
- `payment`
- `notification`
- `auth`
- `security`
- `user`
- `admin`
- `review`
- `waitinglist`
- `files`
- 

Additional supporting modules include authentication, security, reviews, waiting list management, admin reporting, and file storage.

---

## Main Features

### Guest Features
- Browse hotels and room types
- View hotel details
- Check room availability
- Preview booking prices
- Create bookings
- Cancel own bookings
- Make mock payments
- Submit reviews
- Join waiting lists for fully booked room types

### Manager Features
- Manage their hotels and room types
- Confirm and complete bookings
- View hotel bookings
- Upload hotel and room images

### Admin Features
- Full hotel and room management
- Manage pricing rules
- Manage amenities
- Access analytics reports
- Monitor payments and notifications

---

## Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3.x
- **Database:** MySQL
- **Security:** Spring Security + JWT
- **ORM:** Spring Data JPA + Hibernate
- **API Documentation:** Swagger / OpenAPI
- **Testing:** JUnit 5, Mockito, Spring MockMvc
- **Build Tool:** Maven

---

## Project Structure

```text
src/main/java/com/swer313/projectstep1/
│
├── catalog/              # Hotels, room types, amenities
├── availabilitypricing/  # Availability, pricing rules, currency conversion
├── booking/              # Booking creation, cancellation, confirmation
├── payment/              # Payment intents, success/failure simulation, refunds
├── notification/         # Email notifications and retry scheduler
│
├── auth/                 # Registration and login
├── security/             # JWT filter and access control
├── user/                 # Users and roles
├── admin/                # Reports and analytics
├── review/               # Reviews and rating summaries
├── waitinglist/          # Waiting list management
└── files/                # Local image storage