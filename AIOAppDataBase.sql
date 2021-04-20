CREATE DATABASE dbtelcr_aioapp;
USE dbtelcr_aioapp; 
CREATE USER 'dbtelcr_aioapp'@'localhost' IDENTIFIED BY 'aioapptelcreat';
GRANT ALL PRIVILEGES ON * . * TO 'dbtelcr_aioapp'@'localhost';