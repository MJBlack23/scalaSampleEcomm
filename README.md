# Generic ECommerce API

## Routes
* GET /products - list all products

* POST /products - add a new product (requires authentication)

* GET /products/:id - show a product

* PUT /products/:id - update a product (requires auth)

* DELETE /products/:id - remove a product (requires auth)


## Dependencies
* Akka-http (for web api)
* Slick (for database integration)
