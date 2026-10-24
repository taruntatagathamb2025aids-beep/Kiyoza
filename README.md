# Kiyoza: Smart Food Transparency System

A Java Swing desktop app (JDBC + MySQL) where customers can see a restaurant's ingredients,
preparation details, allergens and freshness checks, and staff keep that data up to date.

## What you need
- JDK 17 or newer
- MySQL 8 (Workbench or the command line)
- MySQL Connector/J (the `mysql-connector-j-x.y.z.jar` file from the MySQL downloads page)

## Setup
1. Create the database and sample data:
   `mysql -u root -p < sql/schema.sql`  (or open `sql/schema.sql` in Workbench and run it)
2. Create a `lib` folder in the project root and put the Connector/J jar inside it.
3. If your MySQL password is not empty, copy `db.properties.example` to `db.properties` and edit it.

## Compile and run

Linux / macOS:
```
mkdir -p out
javac -cp "lib/*" -d out $(find src -name "*.java")
java -cp "out:lib/*" kiyoza.Main
```

Windows (Command Prompt):
```
mkdir out
dir /s /b src\*.java > sources.txt
javac -cp "lib/*" -d out @sources.txt
java -cp "out;lib/*" kiyoza.Main
```

In an IDE (IntelliJ / Eclipse / NetBeans): create a project from this folder, mark `src` as the
source root, add the Connector/J jar as a library, and run `kiyoza.Main`.

## Demo logins
| Role  | Email | Password |
|-------|-------|----------|
| Admin | admin@kiyoza.com | admin123 |
| Guest | use "View Menu as Guest" | none |

## Demo script (admin updates, customer sees it)
1. Log in as admin, press **Open Customer Menu** (a second window opens).
2. In the admin window, open **Freshness Checks**, pick *Tomato*, choose *Spoiled*, press **Record Check**.
3. In the customer window press **Refresh** (it also refreshes itself every 10 seconds): dishes
   with tomato now show "Ingredient issue" and the ingredient row turns red.
4. In **Ingredients**, change an allergen (for example set *Basil* to "Tree nuts"). The customer
   window's "Avoid allergen" list and dish details update.
5. In **Menu Items**, untick "Available", press Update: the dish disappears from the customer menu.

## CRUD map
| Table | Create | Read | Update | Delete |
|-------|--------|------|--------|--------|
| food_items + food_ingredients | Menu Items > Add | Menu Items table, customer menu | Menu Items > Update | Menu Items > Delete |
| ingredients | Ingredients > Add | Ingredients table | Ingredients > Update | Ingredients > Delete |
| inventory | Inventory > Save | Inventory table | Inventory > Save (existing ingredient) | Inventory > Delete Record |
| freshness_logs | Freshness Checks > Record Check | Log table, customer detail view | (logs are append-only) | Delete Selected Log |
| users | seeded by schema.sql | Login | n/a | n/a |

## Project layout
```
sql/schema.sql         tables + sample data
src/kiyoza/model       OOP classes (User, Admin, Customer, Restaurant, FoodItem, Ingredient, Inventory, FreshnessLog)
src/kiyoza/dao         JDBC code, one DAO per table group
src/kiyoza/db          DBConnection
src/kiyoza/ui          Swing screens
docs/DESIGN.md         class diagram, ER diagram, OOP/DBMS concept map
```
