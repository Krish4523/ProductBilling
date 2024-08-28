# ProductBilling System (PBS)

ProductBilling is a simple command-line Java application for managing a billing system for a store. It supports operations like adding and removing products, searching for products, viewing the product list, and generating invoices.

## Features

- **View Menu**: Displays the list of available products from the database.
- **Add Product**: Adds a product to the customer's cart.
- **Remove Product**: Removes a product or reduces its quantity in the customer's cart.
- **Search Products**: Searches for a product by its ID.
- **View Product List**: Displays all products in the customer's cart.
- **Print Bill**: Generates and prints a bill for the products in the cart.
- **Exit**: Exits the application.

## Requirements

- Java 8 or above
- MySQL database
- MySQL Connector / JDBC driver

## Setup

1. **Clone the repository**:
    ```bash
    git clone https://github.com/yourusername/ProductBilling.git
    cd ProductBilling
    ```

2. **Database Configuration**:

   - Create a MySQL database named `product_billing`.
   - Create a `products` table with the following schema:
     ```sql
     CREATE TABLE products (
       id INT AUTO_INCREMENT PRIMARY KEY,
       name VARCHAR(255),
       price DOUBLE,
       quantity INT,
       type VARCHAR(50)
     );
     ```
   - Insert some sample data into the `products` table.

3. **Modify Database Credentials**:

   - Update the database URL, username, and password in the `PBS` class:
