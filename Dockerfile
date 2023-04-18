# Use the official Python image as the base image
FROM python:3.11.0

# Set the working directory in the container
WORKDIR /app

# Install system dependencies
RUN apt-get update && \
    apt-get install -y openjdk-11-jdk && \
    apt-get clean

# Install Gradle
ARG GRADLE_VERSION=6.6.1
ARG GRADLE_DOWNLOAD_SHA256=7873ed5287f47ca03549ab8dcb6dc877ac7f0e3d7b1eb12685161d10080910ac
RUN set -o errexit -o nounset \
    && echo "Downloading Gradle" \
    && wget --no-verbose https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip \
    && echo "Checking download hash" \
    && echo "${GRADLE_DOWNLOAD_SHA256} *gradle-${GRADLE_VERSION}-bin.zip" | sha256sum --check - \
    && echo "Installing Gradle" \
    && unzip gradle-${GRADLE_VERSION}-bin.zip \
    && rm gradle-${GRADLE_VERSION}-bin.zip \
    && mv "gradle-${GRADLE_VERSION}" /opt/gradle \
    && ln --symbolic /opt/gradle/bin/gradle /usr/bin/gradle \
    && echo "Testing Gradle installation" \
    && gradle --version

# Install Maven
ARG MAVEN_VERSION=3.9.1
ARG MAVEN_DOWNLOAD_SHA512=d3be5956712d1c2cf7a6e4c3a2db1841aa971c6097c7a67f59493a5873ccf8c8b889cf988e4e9801390a2b1ae5a0669de07673acb090a083232dbd3faf82f3e3
RUN wget --no-verbose https://downloads.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    && echo "${MAVEN_DOWNLOAD_SHA512} *apache-maven-${MAVEN_VERSION}-bin.tar.gz" | sha512sum --check - \
    && tar -xzf apache-maven-${MAVEN_VERSION}-bin.tar.gz -C /opt \
    && rm apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    && ln -s /opt/apache-maven-${MAVEN_VERSION}/bin/mvn /usr/bin/mvn

# Copy the application code
COPY . .

# Copy requirements.txt and install the dependencies
COPY pyproject.toml ./
COPY poetry.lock ./
RUN pip install poetry
RUN poetry config virtualenvs.create false
RUN poetry install --without=dev

# Build the Java project
RUN gradle buildMaven
RUN gradle build

# Expose the port the app runs on
EXPOSE 6875

# Run the application
CMD ["python", "./main.py"]
