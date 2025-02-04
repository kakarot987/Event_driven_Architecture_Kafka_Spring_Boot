terraform {
  required_providers {
    docker = {
      source  = "kreuzwerker/docker"
      version = "~> 3.0"
    }
  }
}

provider "docker" {}

resource "docker_network" "local_network" {
  name = "local_network"
}

resource "docker_image" "java_image" {
  name = "eclipse-temurin:23-jdk"
}

resource "docker_container" "config_server" {
  image = docker_image.java_image.name
  name  = "config-server"

  networks_advanced {
    name = docker_network.local_network.name
  }

  ports {
    internal = 8888
    external = 8888
  }

  env = [
    "SPRING_PROFILES_ACTIVE=local"
  ]
}

