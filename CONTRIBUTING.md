# Contributing to Inventory Management System

We love your input! We want to make contributing to this project as easy and transparent as possible, whether it's:

- Reporting a bug
- Discussing the current state of the code
- Submitting a fix
- Proposing new features
- Becoming a maintainer

## We Develop with Github

We use GitHub to host code, to track issues and feature requests, as well as accept pull requests.

## We Use [Github Flow](https://guides.github.com/introduction/flow/index.html)

Pull requests are the best way to propose changes to the codebase. We actively welcome your pull requests:

1. Fork the repo and create your branch from `main`.
2. If you've added code that should be tested, add tests.
3. If you've changed APIs, update the documentation.
4. Ensure the test suite passes.
5. Make sure your code lints.
6. Issue that pull request!

## Any contributions you make will be under the MIT Software License

In short, when you submit code changes, your submissions are understood to be under the same [MIT License](LICENSE) that covers the project. Feel free to contact the maintainers if that's a concern.

## Report bugs using Github's [issues](https://github.com/tanveer744/Inventory-Management-System/issues)

We use GitHub issues to track public bugs. Report a bug by [opening a new issue](https://github.com/tanveer744/Inventory-Management-System/issues/new); it's that easy!

## Write bug reports with detail, background, and sample code

**Great Bug Reports** tend to have:

- A quick summary and/or background
- Steps to reproduce
  - Be specific!
  - Give sample code if you can
- What you expected would happen
- What actually happens
- Notes (possibly including why you think this might be happening, or stuff you tried that didn't work)

## Development Environment Setup

1. **Prerequisites**
   - Java 21 or higher
   - Maven 3.6 or higher
   - MySQL 8.0+ or PostgreSQL 12+

2. **Setup**
   ```bash
   git clone https://github.com/tanveer744/Inventory-Management-System.git
   cd Inventory-Management-System
   mvn clean install
   ```

3. **Running Tests**
   ```bash
   mvn test
   ```

4. **Running the Application**
   ```bash
   mvn exec:java
   ```

## Code Style

- Follow standard Java conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public methods and classes
- Ensure all tests pass before submitting

## License

By contributing, you agree that your contributions will be licensed under its MIT License.