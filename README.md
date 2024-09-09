# Info

This repository contains the implementation of the prototype that was developed for the bachelor thesis "A PlantUML Based Approach for Static Design Conformance Checking".

# arcucheck

"arcucheck" is a tool to monitor the compliance between design and implementation based on PlantUML class diagrams.

## Execution
To use this tool, you have two options:

1. **Run in an IDE**: Open the project in an IDE like IntelliJ IDEA and execute the main function in the Main.kt class.

2. **Build and Run from the Command Line**: Generate a .jar file by running the command `gradle jar` from the project's root directory, and then execute the .jar file.

## Usage
The tool compares PlantUML (.puml) files, which represent the design, with source code files (.java) that represent the implementation. It can be used to process a single design model or an entire directory of models, depending on the program arguments provided. The .puml files must contain PlantUML class diagrams. Other UML diagram types are not supported. See the [PlantUML reference](https://plantuml.com/de/class-diagram) for information about the PlantUML class diagram structure and syntax.

### Single Model Processing

To use this option, one .puml file for the design and one .java file for the corresponding implementation has to be provided.

#### To use a single .puml file, run:
```
arcucheck -f -dfp <PATH TO .puml FILE> -cp <PATH TO .java FILE>
```
or
```
arcucheck --file --diagramFilePath <PATH TO .puml FILE> --codePath <PATH TO .java FILE>
```
### Directory Processing
For this option, a directory containing the .puml files must be provided. Each .puml file must specify the paths to the related code files by including the following line at the top of the file. Note: This line is a PlantUML comment (comment modifier: ', at the beginning); otherwise, the diagrams will no longer render. 

```
'implementation_path=[<PATH TO IMPLEMENTAION>]
@startuml
....
@enduml
```
 
#### To process a whole directory, run:
```
arcucheck -d -ddp <PATH TO DIRECTORY> 
```
or
```
arcucheck --directory --diagramDirectoryPath <PATH TO DIRECTORY> 
```

## Test Report Generation
The project includes unit tests designed to evaluate the deviation detection capabilities of the prototype developed for a bachelor thesis. To generate a test report, run the following command from the project root:
 ```
  ./gradlew test 
 ```
You can view the test report by opening the `index.html` file located in `build/reports/tests/test/index.html.