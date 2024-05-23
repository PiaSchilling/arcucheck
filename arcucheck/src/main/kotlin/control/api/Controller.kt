package control.api

interface Controller {
    fun onExecuteCommand(args: List<String>)
}