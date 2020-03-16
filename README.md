# Lagom Basket

A simple service representing two operations on a basket using Lagom framework. The service supports two operations,

- Get basket
- Add item

## Notes

- Basket is being associated with a random user UUID upon first addition to an item. In a real world scenario, an Authentication/Authorization microservice should exists. Therefore, a JWT token (or some other token) is supposedly provided and included in the Authorization header in all HTTP requests.
- Item price is being passed while adding to basket. This should be replaced by a call to another service which is responsible on the product catalogue.

## Running the service locally

```mvn lagom:runAll```
