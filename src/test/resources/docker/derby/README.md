#Notes on testing derby v.10.15.1.3 using Docker container

- name: derby
  version: 10.15.1.3
  url: jdbc:derby://localhost:1526/lbcat
  username: lbuser
  password: LiquibasePass1

- name: derby
  version: 10.12.1.1
  url: jdbc:derby://localhost:1527/lbcat
  username: lbuser
  password: LiquibasePass1