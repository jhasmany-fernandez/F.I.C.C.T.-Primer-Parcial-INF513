Proyecto: Servidor Correos - Desktop

```bash
correo-chatbot/
├── .devcontainer/
│   ├── devcontainer.json         <-- VS Code + Java + PostgreSQL client
│   └── Dockerfile
│
├── docker-compose.yml           <-- Define contenedor app + PostgreSQL
│
├── Dockerfile                   <-- Imagen de producción para despliegue (Java + Spring Boot)
│
├── pom.xml                      <-- Proyecto Maven con Spring Boot, Jakarta Mail, PostgreSQL
│
├── src/
│   ├── main/
│   │   ├── java/com/empresa/
│   │   │   ├── CorreoChatbotApplication.java     <-- Main Spring Boot
│   │   │   ├── controller/
│   │   │   │   └── CorreoController.java         <-- Web/API para enviar correos
│   │   │   ├── service/
│   │   │   │   ├── CorreoSMTPService.java        <-- Lógica para enviar correo
│   │   │   │   └── CorreoPOPService.java         <-- Lógica para leer correo (chatbot)
│   │   │   ├── model/
│   │   │   │   └── Cliente.java                  <-- Modelo de ejemplo
│   │   │   ├── repository/
│   │   │   │   └── ClienteRepository.java       <-- Acceso a BD con Spring Data JPA
│   │   │   └── config/
│   │   │       └── ConfigCorreo.java             <-- Lee valores SMTP desde properties
│   │   └── resources/
│   │       ├── templates/index.html              <-- Interfaz web con Thymeleaf
│   │       └── application.properties            <-- Configuración Spring Boot + SMTP + PostgreSQL


```