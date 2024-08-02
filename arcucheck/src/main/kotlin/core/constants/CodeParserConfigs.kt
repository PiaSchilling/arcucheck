package core.constants

const val RELEASE = "release";
const val TEST = "test";

fun getReleaseConfig(tempFilePath: String, codePath: String): Array<String> {
    return arrayOf(
        "-o", // --outfile <arg>          Set the output file
        tempFilePath,
        "-f", // --filepath <arg>         Set the input file/directory
        codePath,
        "-fdef", //--field_default       Add default fields
        "-fpri", //--field_private       Add private fields
        "-fpro", //--field_protected     Add protected fields
        "-fpub", //--field_public        Add public fields
        "-mdef", //--method_default      Add default methods
        "-mpri", //--method_private      Add private methods
        "-mpub", //--method_public       Add public methods
        "-mpro", //--method_protected    Add protected methods
        "-sctr", //--show_constructors   Show constructors
        "-spkg", //--show_package        Show package
    )
}

fun getTestConfig(tempFilePath: String, codePath: String): Array<String> {
    return arrayOf(
        "-o", // --outfile <arg>          Set the output file
        tempFilePath,
        "-f", // --filepath <arg>         Set the input file/directory
        codePath,
        "-fdef", //--field_default       Add default fields
        "-fpri", //--field_private       Add private fields
        "-fpro", //--field_protected     Add protected fields
        "-fpub", //--field_public        Add public fields
        "-mdef", //--method_default      Add default methods
        "-mpri", //--method_private      Add private methods
        "-mpub", //--method_public       Add public methods
        "-mpro", //--method_protected    Add protected methods
        "-sctr", //--show_constructors   Show constructors
    )
}

