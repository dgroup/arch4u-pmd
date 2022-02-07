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

*   [Overview](#overview)

*   [How to use?](#how-to-use)

*   [How to contribute?](#how-to-contribute)

*   [Contributors](#contributors)

### Overview

**arch4u-pmd** is a ...

### How to use?
#### Maven
1. Enable arch4u-pmd rules in `pom.xml`
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
#### Gradle
1. Activate pmd plugin in `build.gradle`
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
        ruleSetFiles = files("your-pmd-ruleset.xml")
        ruleSets = []                                   // Keep it as is, workaround for pmd
    }
    ```
2. Configure `your-pmd-ruleset.xml`
   ```xml
   <?xml version="1.0"?>
   <ruleset name="Your pmd rules"
     xmlns="http://pmd.sourceforge.net/ruleset/2.0.0"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://pmd.sourceforge.net/ruleset/2.0.0 https://pmd.sourceforge.io/ruleset_2_0_0.xsd">
    
     ...
     <rule ref="io/github/dgroup/arch4u/pmd/arch4u-ruleset.xml"/>
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
4. Download pmd rule designer - https://pmd.github.io/latest/pmd_userdocs_extending_designer_reference.html

### Contributors

*   [dgroup](https://github.com/dgroup) as Yurii Dubinka (<yurii.dubinka@gmail.com>)
*   [dykov](https://github.com/dykov) as Oleksii Dykov (<dykovoleksii@gmail.com>)
*   [smithros](https://github.com/smithros) as Rostyslav Koval (<kovalr2000@gmail.com>)
