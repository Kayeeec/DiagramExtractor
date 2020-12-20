# DiagramExtractor

A demo project, showcasing a diagram extraction algorithm. 

## Running and using this demo

- open the project in an IDE (developed with IntelliJ IDEA)
- build
- run `Main.kt`

Some sample Visual Paradigm generated PDFs are located in the `test_files` folder in the content root. 
The program takes and **extracts diagrams from all files in this folder**, so any new sample VP PDF can be added here. 

Diagrams will be **extracted into `extracted_diagrams` folder** in the content root. 

### Problems with Batik dependency

If there is a problem with Batik dependencies during build, just remove the last `batik-all` dependency from `pom.xml`, reload Maven and then add the dependency and reload Maven again. 

```
<dependency>
    <groupId>org.apache.xmlgraphics</groupId>
    <artifactId>batik-all</artifactId>
    <version>1.13</version>
    <type>pom</type>
</dependency>
```
