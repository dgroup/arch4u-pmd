[![Maven](https://img.shields.io/maven-central/v/io.github.dgroup/arch4u-pmd.svg)](https://mvnrepository.com/artifact/io.github.dgroup/arch4u-pmd)
[![Javadocs](http://www.javadoc.io/badge/io.github.dgroup/arch4u-pmd.svg)](http://www.javadoc.io/doc/io.github.dgroup/arch4u-pmd)
[![License: MIT](https://img.shields.io/github/license/mashape/apistatus.svg)](./license.txt)
[![Commit activity](https://img.shields.io/github/commit-activity/y/dgroup/arch4u-pmd.svg?style=flat-square)](https://github.com/dgroup/arch4u-pmd/graphs/commit-activity)
[![Hits-of-Code](https://hitsofcode.com/github/dgroup/arch4u-pmd)](https://hitsofcode.com/view/github/dgroup/arch4u-pmd)

[![CI](https://github.com/dgroup/arch4u-pmd/actions/workflows/build.yml/badge.svg)](https://github.com/dgroup/arch4u-pmd/actions/workflows/build.yml)
[![0pdd](http://www.0pdd.com/svg?name=dgroup/arch4u-pmd)](http://www.0pdd.com/p?name=dgroup/arch4u-pmd)
[![Dependency Status](https://requires.io/github/dgroup/arch4u-pmd/requirements.svg?branch=master)](https://requires.io/github/dgroup/arch4u-pmd/requirements/?branch=master)
[![Known Vulnerabilities](https://snyk.io/test/github/dgroup/arch4u-pmd/badge.svg)](https://app.snyk.io/org/dgroup/project/c8b51bb3-7683-41c8-9a4e-b32a6f9069b6)

[![DevOps By Rultor.com](http://www.rultor.com/b/dgroup/arch4u-pmd)](http://www.rultor.com/p/dgroup/arch4u-pmd)
[![EO badge](http://www.elegantobjects.org/badge.svg)](http://www.elegantobjects.org/#principles)
[![We recommend IntelliJ IDEA](http://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![Qulice](https://img.shields.io/badge/qulice-passed-blue.svg)](http://www.qulice.com/)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=dgroup_arch4u-pmd&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=dgroup_arch4u-pmd)
[![codebeat badge](https://codebeat.co/badges/07852d4a-459c-4775-949d-833e3eeebcfe)](https://codebeat.co/projects/github-com-dgroup-arch4u-pmd-master)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/53d3d211de354b45a06aaa82dcf432b5)](https://www.codacy.com/gh/dgroup/arch4u-pmd/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dgroup/arch4u-pmd&amp;utm_campaign=Badge_Grade)
[![Codecov](https://codecov.io/gh/dgroup/arch4u-pmd/branch/master/graph/badge.svg)](https://codecov.io/gh/dgroup/arch4u-pmd)

* [Overview](#overview)
* [How to use?](#how-to-use)
* [How to contribute?](#how-to-contribute)
* [Contributors](#contributors)

### Overview

**arch4u-pmd** is a library with pmd rules that bring new regulations related to known problems in
REST API, logging, monitoring, etc., including reconfigured default pmd
rules to decrease false-positive
violations during usage of well-known frameworks like Spring, Quarkus, etc.

In addition to our custom/reconfigured rules we are using the latest stable pmd-java version which is `6.42.0` with more than [320+ rules](https://pmd.github.io/pmd-6.42.0/pmd_rules_java.html) with default configuration.

*Legend*

⌛ - planned for review considering framework(s) architecture, ✅ - included
in `arch4u-ruleset.xml`, 🌵 - temporary disabled or reconfigured in `arch4u-ruleset.xml`, ❌ -
disabled/not planned in `arch4u-ruleset.xml`

| PMD rule                                                                                                                               |      Provider      | Status | Spring | Quarkus |
|:------------------------------------------------------------------------------------------------------------------------------------------|:------------------:|:------:|:------:|:-------:|
| [UseExistingMediaTypeConstant](https://github.com/dgroup/arch4u-pmd/discussions/43)                                                       | `arch4u-pmd:0.1.0` |   ✅    |   ✅    |    ⌛    |
| [UseOpenApiInRestEndpoints](https://github.com/dgroup/arch4u-pmd/discussions/73)                                                          | `arch4u-pmd:0.1.0` |   ✅    |   ✅    |    ⌛    |
| [RestEndpointsWithoutExposedMetrics](https://github.com/dgroup/arch4u-pmd/discussions/74)                                                 | `arch4u-pmd:0.1.0` |   ✅    |   ✅    |    ⌛    |
| [UseConstantAsMetricName](https://github.com/dgroup/arch4u-pmd/discussions/75)                                                            | `arch4u-pmd:0.1.0` |   ✅    |   ✅    |    ⌛    |
| [NoMandatoryConstructorInExceptionClass](https://github.com/dgroup/arch4u-pmd/discussions/31)                                             | `arch4u-pmd:0.1.0` |   ✅    |   ✅    |    ⌛    |
| [AvoidUsingObjectMapperAsALocalVariable](https://github.com/dgroup/arch4u-pmd/discussions/30)                                             | `arch4u-pmd:0.1.0` |   ✅    |   ✅    |    ⌛    |
| [AvoidMdcOutsideTryStatement](https://github.com/dgroup/arch4u-pmd/discussions/86)                                                        | `arch4u-pmd:0.1.0` |   ✅    |   ✅    |    ⌛    |
| [PotentiallyThreadLocalPollutionByMdc](https://github.com/dgroup/arch4u-pmd/discussions/88)                                               | `arch4u-pmd:0.1.0` |   ✅    |   ✅    |    ⌛    |
| [GuardLogStatement](https://pmd.github.io/latest/pmd_rules_java_bestpractices.html#guardlogstatement)                                     | `pmd-java:6.42.0`  |   ❌    |   ⌛    |    ⌛    |
| [JUnitAssertionsShouldIncludeMessage](https://pmd.github.io/latest/pmd_rules_java_bestpractices.html#junitassertionsshouldincludemessage) | `pmd-java:6.42.0`  |   🌵   |   ⌛    |    ⌛    |
| [UnusedPrivateMethod](https://pmd.github.io/latest/pmd_rules_java_bestpractices.html#unusedprivatemethod)                                 | `pmd-java:6.42.0`  |   🌵   |   ⌛    |    ⌛    |
| [AtLeastOneConstructor](https://pmd.github.io/latest/pmd_rules_java_codestyle.html#atleastoneconstructor)                                 | `pmd-java:6.42.0`  |   🌵   |   ✅    |    ⌛    |
| [OnlyOneReturn](https://pmd.github.io/latest/pmd_rules_java_codestyle.html#onlyonereturn)                                                 | `pmd-java:6.42.0`  |   🌵   |   ⌛    |    ⌛    |
| [CommentRequired](https://pmd.github.io/latest/pmd_rules_java_documentation.html#commentrequired)                                         | `pmd-java:6.42.0`  |   🌵   |   ⌛    |    ⌛    |
| [AvoidCatchingGenericException](https://pmd.github.io/latest/pmd_rules_java_design.html#avoidcatchinggenericexception)                    | `pmd-java:6.42.0`  |   ⌛    |   ⌛    |    ⌛    |
| [CouplingBetweenObjects](https://pmd.github.io/latest/pmd_rules_java_design.html#couplingbetweenobjects)                                  | `pmd-java:6.42.0`  |   🌵   |   ⌛    |    ⌛    |
| [LawOfDemeter](https://pmd.github.io/latest/pmd_rules_java_design.html#lawofdemeter)                                                      | `pmd-java:6.42.0`  |   ❌    |   ⌛    |    ⌛    |
| [LoosePackageCoupling](https://pmd.github.io/latest/pmd_rules_java_design.html#loosepackagecoupling)                                      | `pmd-java:6.42.0`  |   ⌛    |   ⌛    |    ⌛    |
| [SignatureDeclareThrowsException](https://pmd.github.io/latest/pmd_rules_java_design.html#signaturedeclarethrowsexception)                | `pmd-java:6.42.0`  |   ⌛    |   ⌛    |    ⌛    |
| [TooManyFields](https://pmd.github.io/latest/pmd_rules_java_design.html#toomanyfields)                                                    | `pmd-java:6.42.0`  |   🌵   |   ⌛    |    ⌛    |
| [TooManyMethods](https://pmd.github.io/latest/pmd_rules_java_design.html#toomanymethods)                                                  | `pmd-java:6.42.0`  |   🌵   |   ⌛    |    ⌛    |
| [UseObjectForClearerAPI](https://pmd.github.io/latest/pmd_rules_java_design.html#useobjectforclearerapi)                                  | `pmd-java:6.42.0`  |   ❌    |   ⌛    |    ⌛    |

### How to use?

#### Maven (pom.xml)

 ```xml
 ...
<build>
  <plugins>
    ...
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-pmd-plugin</artifactId>
      <version>3.15.0</version>
      <executions>
        <execution>
          <phase>test</phase>
          <goals>
            <goal>check</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <printFailingErrors>true</printFailingErrors>
        <rulesets>
          ...
          <ruleset>io/github/dgroup/arch4u/pmd/arch4u-ruleset.xml</ruleset>
          ...
        </rulesets>
        <excludeRoots>
          <excludeRoot>target/generated-sources/</excludeRoot>
        </excludeRoots>
      </configuration>
      <dependencies>
        <!-- Latest arch4u-rules -->
        <dependency>
          <groupId>io.github.dgroup</groupId>
          <artifactId>arch4u-pmd</artifactId>
          <version>${version}</version>
        </dependency>
      </dependencies>
    </plugin>
    ...
  </plugins>
</build>
  ...
 ```

#### Gradle (build.gradle)

 ```groovy
 apply plugin: 'pmd'

dependencies {
    ...
    pmd "io.github.dgroup:arch4u-pmd:${version}"    // use latest arch4u-pmd rules version
    pmd "commons-io:commons-io:2.11.0"              // required dependency by pmd engine
    ...
}

pmd {
    consoleOutput = true
    ruleSetFiles = files("io/github/dgroup/arch4u/pmd/arch4u-ruleset.xml")
    ruleSets = []                                   // Keep it as is, workaround for pmd
}
 ```

#### Include arch4u-pmd rules into your existing custom ruleset

0. Don't forget to add rules to classpath in Maven/Gradle pmd plugin (see lines above)
1. Let's assume that you already have pmd rules defined in `your-pmd-ruleset.xml`
2. Add line `<rule ref="io/github/dgroup/arch4u/pmd/arch4u-ruleset.xml"/>` to `your-pmd-ruleset.xml`
   ```xml
   <?xml version="1.0"?>
   <ruleset name="pmd ruleset with your rules"
     xmlns="http://pmd.sourceforge.net/ruleset/2.0.0"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://pmd.sourceforge.net/ruleset/2.0.0 https://pmd.sourceforge.io/ruleset_2_0_0.xsd">
    
     ...
     <rule ref="io/github/dgroup/arch4u/pmd/arch4u-ruleset.xml"/>
     ...

   </ruleset>
   ```

#### Exclude particular rule

```xml
<?xml version="1.0"?>
<ruleset name="pmd ruleset with your rules">
  ...
  <rule ref="io/github/dgroup/arch4u/pmd/arch4u-ruleset.xml">
    <exclude name="UseExistingMediaTypeConstant"/>
  </rule>
  ...
</ruleset>
```

#### Reconfigure a rule

```xml
<?xml version="1.0"?>
<ruleset name="pmd ruleset with your rules">
  ...
  <!-- 1. Exclude rule with default configuration  -->
  <rule ref="io/github/dgroup/arch4u/pmd/arch4u-ruleset.xml">
    <exclude name="UseExistingMediaTypeConstant"/>
  </rule>
  <!-- 2. Reconfigure rule with expected property -->
  <rule name="UseExistingMediaTypeConstant"
        language="java"
        externalInfoUrl="https://github.com/dgroup/arch4u-pmd/discussions/43"
        message="Use existing MediaType constant instead of string literal: https://github.com/dgroup/arch4u-pmd/discussions/43"
        class="io.github.dgroup.arch4u.pmd.UseExistingConstant">
    <priority>3</priority>
    <properties>
      <!-- 3. Set the 'regexPattern' considering your needs -->
      <property name="regexPattern" description="Regular expression of prohibited string"
                value="(^|\s)(application\/(json|xml|atom\+xml|x-www-form-urlencoded|octet-stream|svg\+xml|xhtml\+xml)|(multipart\/form-data)|(text\/(html|xml|plain)))(\s|$)"/>
    </properties>
  </rule>
  ...
</ruleset>
```

### How to contribute?

[![EO badge](http://www.elegantobjects.org/badge.svg)](http://www.elegantobjects.org/#principles)

1. Pull requests are welcome! Don't forget to add your name to contribution section and run this,
   beforehand:
    ```bash
    mvn -Pqulice clean install
    ```
2. Everyone interacting in this project’s codebases, issue trackers, chat rooms is expected to
   follow the [code of conduct](.github/code_of_conduct.md).
3. Latest maven coordinates [here](https://github.com/dgroup/arch4u-pmd/releases):
    ```xml
    <dependency>
        <groupId>io.github.dgroup</groupId>
        <artifactId>arch4u-pmd</artifactId>
        <version>${version}</version>
    </dependency>
    ```
4. Download pmd rule designer
    - https://pmd.github.io/latest/pmd_userdocs_extending_designer_reference.html

### Contributors

* [dgroup](https://github.com/dgroup) as Yurii Dubinka (<yurii.dubinka@gmail.com>)
* [dykov](https://github.com/dykov) as Oleksii Dykov (<dykovoleksii@gmail.com>)
* [smithros](https://github.com/smithros) as Rostyslav Koval (<kovalr2000@gmail.com>)
