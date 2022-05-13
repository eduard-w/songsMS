# songsMS

This project is a microservice-based Spring Cloud application to manage user-generated song lists and song files. It was part of an university assignment for component-based software engineering.

The app is split into five components corresponding to their tasks:  
Component|Task
:-:|:-:
gateway|forward incoming api calls to other components
registry|discover and provide access to all components
auth|log in and verify user accounts
songs|manage user-provided songs and songlists
download|manage user-provided download files for songs
