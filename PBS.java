package ProductBilling;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.Scanner;

public class PBS {
    private static Connection con = null;
    private static PreparedStatement pst = null;
    public static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        ProductBilling pb = new ProductBilling();
        Class.forName("com.mysql.cj.jdbc.Driver");
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/product_billing", "root", "");
        if (con != null) {
            System.out.println("Connection Established to the DataBase");
        } else {
            System.out.println("Connection Failed!!");
        }
        con.setAutoCommit(false);
        System.out.print("Enter Customer Name : ");
        pb.customer = sc.nextLine();
        int id, quantity;
        String p_name = "";
        double price = 0.0;
        boolean isPrinted = false;
        while (true) {
            try {
                System.out.println("""
                        0 : View Menu
                        1 : Add Product
                        2 : Remove Product
                        3 : Search Products
                        4 : View Product List
                        5 : Print Bill
                        6 : Exit
                        """);
                System.out.print("Enter Choice : ");
                int choice = Integer.parseInt(sc.nextLine().trim());

                switch (choice) {
                    case 0:
                        pst = con.prepareStatement("select * from products");
                        printMenu(pst.executeQuery());
                        break;
                    case 1:
                        pst = con.prepareStatement("select * from products where type=?");
                        String category = null;
                        while (category == null) {
                            category = selectCategory();
                        }
                        pst.setString(1, category);
                        printMenu(pst.executeQuery());

                        System.out.println("Enter Product Details------");
                        pst = con.prepareStatement("select name,price from products where id=? AND type='" + category + "'");
                        System.out.print("Product ID From Menu : ");
                        id = sc.nextInt();
                        System.out.print("Product Quantity     : ");
                        quantity = sc.nextInt();
                        pst.setInt(1, id);
                        ResultSet rs = pst.executeQuery();
                        while (rs.next()) {
                            p_name = rs.getString(1);
                            price = rs.getDouble(2);
                        }
                        sc.nextLine(); // Consume New Line
                        pb.addProduct(id, p_name, quantity, price);
                        break;

                    case 2:
                        pb.displayProductList(); // to view products stored in List
                        System.out.print("Enter Product ID to Remove It : ");
                        id = sc.nextInt();
                        System.out.print("Enter Quantity to Remove : ");
                        quantity = sc.nextInt();
                        sc.nextLine();
                        pb.removeProduct(id, quantity);
                        break;

                    case 3:
                        System.out.print("Enter Product Id : ");
                        id = sc.nextInt();
                        sc.nextLine();
                        pb.searchProduct(id);
                        break;

                    case 4:
                        pb.displayProductList();
                        break;

                    case 5:
                        try {
                            if (pb.first != null) {
                                pb.printBill();
                                con.commit();
                                isPrinted = true;
                            } else {
                                isPrinted = false;
                                con.rollback();
                                throw new IOException("Printing Error!!!!!");
                            }
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                        break;

                    case 6:
                        try {
                            if (pb.first != null && !isPrinted) {
                                System.out.println("""
                                        1 : Print Bill & Exit
                                        2 : Exit
                                        """);
                                int ch = sc.nextInt();
                                sc.nextLine();
                                if (ch == 1) {
                                    pb.printBill();
                                    con.commit();
                                } else if (ch == 2) {
                                    pb.first = null;
                                    con.rollback();
                                } else {
                                    System.out.println("Enter Valid Choice !!");
                                }
                            }
                            System.out.println("-------------------- Thanks , Visit Again --------------------");
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                        con.close();
                        sc.close();
                        System.exit(0);

                    default:
                        System.out.println("Enter Valid Choice !!");
                        break;
                }
            } catch (NumberFormatException | InputMismatchException e) {
                System.out.println("Invalid Input Try Again !!");
            } catch (Exception e) {
                System.out.println("ERROR : " + e.getMessage());
                System.out.println("Please Try Again !!");
            }
        }
    }

    /**
     * @return boolean value that enough quantity in the database is available or
     * not for particular product
     */
    protected static boolean isEnoughQuantity(int id, int qnt) throws SQLException {
        pst = con.prepareStatement("update products set quantity=quantity-" + qnt + " where id=" + id + " and quantity>=" + qnt);
        int update = pst.executeUpdate();
        if (update > 0) {
            return true;
        } else {
            System.out.println("Not Enough Quantity Available !!");
            return false;
        }
    }

    /**
     * @return boolean value that Stock is updated in the database or not for
     * particular product
     */
    protected static boolean updateStock(int id, int qnt) throws SQLException {
        pst = con.prepareStatement("update products set quantity=quantity+" + qnt + " where id=" + id);
        int update = pst.executeUpdate();
        return update > 0;
    }

    /**
     * prints product list of selected category in menu format
     *
     * @param rs ResultSet of selected category from database
     */
    protected static void printMenu(ResultSet rs) throws SQLException {
        System.out.println("----------------------------------------------------------------------------------");
        System.out.println("ID \t PRODUCT NAME \t\t  QUANTITY   \t    PRICE \t CATEGORY");
        System.out.println("----------------------------------------------------------------------------------");
        while (rs.next()) {
            int id = rs.getInt(1);
            String pName = rs.getString(2);
            int qnt = rs.getInt(4);
            double price = rs.getDouble(3);
            String category = rs.getString(5);
            System.out.printf("%-3d \t %-20s \t    %3d \t %9.2f \t %-10s\n", id, pName, qnt, price, category);
        }
        System.out.println("----------------------------------------------------------------------------------");
    }

    /**
     * To ask user for Product Category from the DataBase
     *
     * @return selected category by user
     */
    private static String selectCategory() {
        System.out.println("""
                \n-------------- PRODUCT CATEGORIES --------------
                1 : Grocery
                2 : Electronics
                3 : Stationary
                4 : Clothes
                5 : Snacks
                6 : Footwear
                7 : Dairy Products
                8 : Furniture
                9 : Appliances
                """);
        System.out.print("Select Category : ");
        return switch (sc.nextInt()) {
            case 1 -> "Grocery";
            case 2 -> "Electronics";
            case 3 -> "Stationary";
            case 4 -> "Clothes";
            case 5 -> "Snacks";
            case 6 -> "Footwear";
            case 7 -> "Dairy Products";
            case 8 -> "Furniture";
            case 9 -> "Appliances";
            default -> {
                System.out.println("Enter Valid Category !!");
                yield null;
            }
        };
    }
}

class ProductBilling {
    String customer;

    static class Product {
        Product link;
        int p_id;
        String p_name;
        private int p_qnt;
        double p_price, p_totalPrice;

        /**
         * Creates a newNode of Linked-List to add in Product-List
         *
         * @param p_id    ID of Product
         * @param p_name  Name of Product
         * @param p_qnt   Quantity of Product
         * @param p_price Price of product
         */
        public Product(int p_id, String p_name, int p_qnt, double p_price) {
            this.link = null;
            this.p_id = p_id;
            this.p_name = p_name;
            this.p_qnt = p_qnt;
            this.p_price = p_price;
        }

        /**
         * @return details of particular Product-Node from the Linked-list
         */
        @Override
        public String toString() {
            return "Product [ID = " + p_id + ", Name = " + p_name + ", Quantity = " + p_qnt + ", Price = " + p_price + ", Total = " + (p_totalPrice = p_price * p_qnt) + "]";
        }

        /**
         * @return quantity of particular Product from the Product-list
         */
        public int getP_qnt() {
            return p_qnt;
        }

        /**
         * To set quantity of existing Product in the Linked-List
         */
        public void setP_qnt(int p_qnt) {
            this.p_qnt = p_qnt;
        }

    }

    Product first = null;

    /**
     * To add new ProductNode in the Linked-list of Product
     */
    void addProduct(int p_id, String p_name, int p_qnt, double p_price) throws SQLException {
        if (!PBS.isEnoughQuantity(p_id, p_qnt)) {
            return;
        }
        Product p = repeatProduct(p_id, p_name);
        if (p != null) {
            int q = p_qnt + p.getP_qnt();
            p.setP_qnt(q);
            System.out.println("Product " + p_name + " added in the List.");
            return;
        }

        // add Last in Product List
        Product product = new Product(p_id, p_name, p_qnt, p_price);
        if (first == null) {
            first = product;
        } else {
            Product temp = first;
            while (temp.link != null) {
                temp = temp.link;
            }
            temp.link = product;
        }
        System.out.println("Product " + p_name + " added in the List.");
    }

    /**
     * To remove product from Linked-List
     */
    void removeProduct(int p_id, int qnt) throws SQLException {
        if (first == null) {
            System.out.println("Product List is Empty !!!");
        } else if (first.p_id == p_id) {
            if (first.p_qnt < qnt) {
                System.out.println("Enter valid Quantity!!!");
                return;
            }
            first.setP_qnt(first.p_qnt - qnt);
            if (PBS.updateStock(p_id, qnt) && first.p_qnt == 0) {
                first = first.link;
                System.out.println("Product Removed Successfully");
            }
        } else {
            Product temp = first;
            while (temp.link != null && temp.link.p_id != p_id) {
                temp = temp.link;
            }
            if (temp.link != null) {
                if (temp.link.p_qnt < qnt) {
                    System.out.println("Enter valid Quantity!!!");
                    return;
                }
                temp.link.setP_qnt(temp.link.p_qnt - qnt);
                if (PBS.updateStock(p_id, qnt) && temp.link.p_qnt == 0) {
                    temp.link = temp.link.link;
                    System.out.println("Product Removed Successfully");
                }
            } else {
                System.out.println("Product Not Found!!!");
            }
        }
    }

    /**
     * To search the Product from Linked-List by p_id
     */
    void searchProduct(int p_id) {
        if (first == null) {
            System.out.println("Product List is Empty !!!");
        } else if (first.p_id == p_id) {
            System.out.println(first);
        } else {
            Product temp = first;
            while (temp != null) {
                if (temp.p_id == p_id) {
                    System.out.println(temp);
                    return;
                }
                temp = temp.link;
            }
            System.out.println("Product Not found");
        }
    }

    /**
     * To display Product-List
     */
    void displayProductList() {
        Product temp = first;
        if (temp == null) {
            System.out.println("Product List is Empty !!!");
        } else {
            System.out.println("Product List----------------------------------------------------------------------");
            while (temp != null) {
                System.out.println(temp);
                temp = temp.link;
            }
            System.out.println("----------------------------------------------------------------------------------");
        }
    }

    /**
     * @return Product node of existing Product otherwise null
     */
    private Product repeatProduct(int p_id, String p_name) {
        Product temp = first;
        while (temp != null) {
            if (temp.p_id == p_id && temp.p_name.equalsIgnoreCase(p_name)) {
                return temp;
            }
            temp = temp.link;
        }
        return null;
    }

    /**
     * @param customerName name of customer
     * @param pw           to Print Header of Bill in txt file
     */
    static void billHeader(String customerName, PrintWriter pw) {
        pw.println("\t\t\t\t\t--------------------Invoice--------------------");
        pw.println("\t\t\t\t\t\t    " + " Krishna Grocery Shop");
        pw.println("\t\t\t\t\t\t      SG Highway, Ahmedabad");
        pw.println("\tGST_ID: 03APP4576K592" + "\t\t\t\t\t\t\t  Contact: +91 7600735432");
        SimpleDateFormat date = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat time = new SimpleDateFormat("hh:mm");
        Date d = new Date();
        pw.printf("\tCustomer: %-20s \t\t\t\t\t\t  ", customerName);
        pw.print(date.format(d) + "  " + time.format(d));
        pw.flush();
    }

    // displayFormat of bill
    static void billFormat(PrintWriter pw) {
        pw.format("\n--------------------------------------------------------------------------------------------------------------------");
        pw.print("\nPRODUCT ID \t\tPRODUCT\t\t\t\tQUANTITY\t\tRATE \t\t\tTOTAL PRICE\n");
        pw.format("--------------------------------------------------------------------------------------------------------------------\n");
        pw.flush();
    }

    /**
     * To Print Bill in txt file
     */
    public void printBill() throws IOException {
        if (first == null) {
            throw new IOException("Printing Error !!!");
        }
        PrintWriter pw = new PrintWriter(new FileWriter(customer + ".txt", false));
        billHeader(customer, pw);
        billFormat(pw);
        Product temp = first;
        double overAllPrice = 0.0, discount, subtotal, sgst, cgst;
        while (temp != null) {
            overAllPrice += temp.p_totalPrice;
            pw.printf(" %-9d              %-20s         	%5d               %9.2f             %14.2f\n", temp.p_id, temp.p_name, temp.p_qnt, temp.p_price, temp.p_totalPrice);
            temp = temp.link;
        }
        pw.print("--------------------------------------------------------------------------------------------------------------------");
        // display overall price
        pw.println("\nTotal Amount  : \t\t\t\t\t\t\t\t\t\t\t " + overAllPrice);
        // calculate & display discount
        discount = overAllPrice * 5 / 100;
        pw.println("\nDiscount      :\t\t\t\t\t\t\t\t\t\t\t         5 %");
        pw.printf("\nYou Save      : \t\t\t\t\t\t\t\t\t\t         %-10.2f\n", discount);
        // display total amount with discount
        subtotal = overAllPrice - discount;
        pw.printf("\nSubtotal      : \t\t\t\t\t\t\t\t\t\t         %-10.2f\n", subtotal);
        // calculate tax
        sgst = overAllPrice * 6 / 100;
        pw.printf("\nSGST          : \t\t\t\t\t\t\t\t\t\t         %-10.2f\n", sgst);
        cgst = overAllPrice * 6 / 100;
        pw.printf("\nCGST          : \t\t\t\t\t\t\t\t\t\t         %-10.2f\n", cgst);
        // calculate & display net payable amount
        pw.printf("\nInvoice Total : \t\t\t\t\t\t\t\t\t\t         %-10.2f\n", (subtotal + cgst + sgst));
        pw.println("--------------------------------------------------------------------------------------------------------------------\n");
        pw.println("\t\t\t\t----------------Thank You for Shopping!!-----------------");
        pw.println("\t\t\t\t          ----------- Visit Again -----------");

        // Just for FUN
        Printing p = new Printing();
        p.start();
        try {
            p.join();
            System.out.println("\nPrinting Successful");
        } catch (InterruptedException ignored) {
        }
        pw.close();
    }
}

// Just for FUN
class Printing extends Thread {
    public void run() {
        int i = (int) (Math.random() * 30);
        System.out.print("Printing Under Process.");
        while (i > 0) {
            System.out.print(".");
            i--;
            try {
                Thread.sleep(300);
            } catch (InterruptedException ignored) {
            }
        }

    }
}
