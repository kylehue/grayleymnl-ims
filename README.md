# Inventory Management System for GreyleyMNL

This application is designed to simplify inventory management processes for GreyleyMNL company, allowing users to efficiently add, delete, and update products within the inventory, hence the inventory management system.

![Sample Drawer](https://raw.githubusercontent.com/kylehue/grayleymnl-ims/master/src/main/resources/images/preview.png)

## Disclaimer
This application, developed for the GreyleyMNL company, is created as a school project for academic purposes only. It is not intended for commercial use. All features and functionalities are designed to meet academic requirements and demonstrate skills acquired during the course of study.

## Technologies Used
- JavaFX
- PostgreSQL
- jBCrypt

## Setup
1. Clone the repository
2. Install dependencies using `mvn install`
3. Add [environment variables](#environment-variables)
4. Run

## Environment Variables
A file named `env.properties` must be located in `/src/main/resources/` with the following properties:
- `mail.email` - Email address used for mailing.
- `mail.password` - Email password used for mailing.
- `database.url` - Database url (local)
- `database.username` - Database username (local)
- `database.password` - Database password (local)
- `database.url.hosted` - Database url (hosted)
- `database.username.hosted` - Database username (hosted)
- `database.password.hosted` - Database url (hosted)
