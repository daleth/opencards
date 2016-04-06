package info.opencards.md

import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.CompositeASTNode
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.LinkMap
import org.intellij.markdown.parser.MarkdownParser
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths


fun main(args: Array<String>) {
    //        String text= "## hellp\n" +
    //                "content" +
    //                "\n" +
    //                "### other section\n" +
    //                "\n" +
    //                "more conetnt\n";

    val text = readFile("/Users/holger/projects/opencards/oc2/testdata/kotlin_qa.md", Charset.defaultCharset())

    parseMD(File("/Users/holger/projects/opencards/oc2/testdata/kotlin_qa.md"))
}

fun parseMD(file: File): List<Pair<String, String>> {
    val text = readFile(file.absolutePath, Charset.defaultCharset())

    val markdownParser = MarkdownParser(GFMFlavourDescriptor())
    val parsedTree = markdownParser.buildMarkdownTreeFromString(text)


    parsedTree.children[0]


    val html = makeHtml(parsedTree, text)
    System.err.println("html is :\n" + html)

    parsedTree.children.filter { it is CompositeASTNode }.forEach { println("$it : ${makeHtml(it, text)}") }

    var blockCounter = 0
    val sections = parsedTree.children.map { makeHtml(it, text) }.groupBy {
        if (it.startsWith("<h")) {
            blockCounter += 1
        }

        blockCounter
    }.map { it.value }

    // try to extract the questions
    val cards = sections.
            // rermove empty sections
            filter { it.size > 1 }.
            // create question to answer map
            map { it.first() to it.joinToString("\n") }

    return cards
}

internal fun makeHtml(parsedTree: ASTNode, text: String): String {
    val htmlGeneratingProviders = GFMFlavourDescriptor().createHtmlGeneratingProviders(LinkMap.buildLinkMap(parsedTree, text), File(".").toURI())

    val html = HtmlGenerator(text, parsedTree, htmlGeneratingProviders, true).generateHtml()
    return html
}

@Throws(IOException::class)
internal fun readFile(path: String, encoding: Charset): String {
    val encoded = Files.readAllBytes(Paths.get(path))
    return String(encoded, encoding)
}
