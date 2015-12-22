package uk.me.jeffsutton.pojogen;/*
 * Copyright (c) 2015 Jeff Sutton.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import com.github.underscore.Function1;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;


/**
 * <p>Class to parse XMl files and generate Java class models annotated for use with SimpleXML.</p>
 * <p>
 * Created by jeff on 03/12/2015.
 */
public class SimplePOJO {

    private static final String[] RESERVED_WORDS = {"default", "class", "public", "void"};
    private final String packageName;
    HashMap<String, XClass> classes = new HashMap<>();
    private String rootTageName;
    private int indentLevel = 0;

    public SimplePOJO(String packageName) {
        this.packageName = packageName;
    }

    public static void main(String[] argv) {
        String packageString = null;

        BufferedReader source = null;

        for (String a : argv) {
            if (a.startsWith("-p:")) {
                packageString = a.substring(3);
                System.out.println("Using package: " + packageString);
            }

            if (a.startsWith("-u:")) {
                try {
                    URL oracle = new URL(a.substring(3));
                    System.out.println("Using URL: " + oracle.toExternalForm());
                    source = new BufferedReader(
                            new InputStreamReader(oracle.openStream(), StandardCharsets.UTF_8), 4096);
                } catch (Exception err) {
                    err.printStackTrace();
                }
            } else {
                source = new BufferedReader(new StringReader(""));
            }
        }

        if (source == null) {
            source = new BufferedReader(new StringReader(""));
        }

        System.out.println("\n\n");

        try {
            SimplePOJO simplePOJO = new SimplePOJO(packageString);
            String r = simplePOJO.generate(source);
            System.out.println(r);

            writeToFile(r, simplePOJO.rootTageName);

        } catch (Exception eek) {
            eek.printStackTrace();
        }

//        try {
//            Serializer serializer = new Persister();
//            for (String a : argv) {
//                if (a.startsWith("-u:")) {
//                    try {
//                        URL oracle = new URL(a.substring(3));
//                        System.out.println("Using URL: " + oracle.toExternalForm());
//                        source = new BufferedReader(
//                                new InputStreamReader(oracle.openStream(), StandardCharsets.UTF_8), 4096);
//                    } catch (Exception err) {
//                        err.printStackTrace();
//                    }
//                } else {
//                    source = new BufferedReader(new StringReader(""));
//                }
//            }
//            Tv html = serializer.read(Tv.class, source, false);
//            Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
//            System.out.println("\n\n\n" + gson.toJson(html, Tv.class));
//        } catch (Exception err) {
//            err.printStackTrace();
//        }
    }

    private static void writeToFile(String data, String rootTageName) {
        try {
            PrintWriter out = new PrintWriter(mkClassName(rootTageName) + ".java");
            out.println(data);
            out.close();
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public static String reservedCheck(String str) {
        if (ArrayUtils.contains(RESERVED_WORDS, str)) {
            return "_" + str;
        }
        return str;
    }

    public static String mkClassName(String name) {
        name = getLastDotInList(name);
        name = name.replace("-", "_");
        name = name.replace("#", "");
        if (name.endsWith("_")) {
            name = name.substring(0, name.length() - 1);
        }
        while (name.contains("_")) {
            int pos = name.indexOf("_");
            name = name.substring(0, pos) + name.substring(pos + 1, pos + 2).toUpperCase() + name.substring(pos + 2);
        }
        return reservedCheck(name);
    }

    public static String getLastDotInList(String str) {
        if (str.indexOf('.') > -1) {
            String[] sr = str.split("/`./");
            str = "";
            for (int i = 0; i < sr.length; i++) {
                sr[i] = cap(sr[i]);
                str += sr[i];
            }
        } else {
            str = cap(str);
        }
        return str;
    }

    public static String cap(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static <String, XField> Collection<XField> values(final Map<String, XField> object) {
        return object.values();
    }

    public static <String, E> Map<String, List<Node>> groupBy(final NodeList iterable, final Function1<Node, String> func) {
        final Map<String, List<Node>> retVal = newLinkedHashMap();
        for (int i = 0; i < iterable.getLength(); i++) {
            Node e = iterable.item(i);
            final String key = func.apply(e);
            List<Node> val;
            if (retVal.containsKey(key)) {
                val = retVal.get(key);
            } else {
                val = newArrayList();
            }
            val.add(e);
            retVal.put(key, val);
        }
        return retVal;
    }

    @SuppressWarnings("unchecked")
    protected static <K, E> Map<K, E> newLinkedHashMap() {
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    protected static <T> List<T> newArrayList() {
        return new ArrayList<>();
    }

    public String getMainClassName() {
        return mkClassName(rootTageName);
    }

    public String pretyFormat(String src) {
        try {
            Source xmlInput = new StreamSource(new StringReader(src));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();

            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            throw new RuntimeException(e); // simple exception handling, please review it
        }
    }

    public String generate(BufferedReader xml) throws ParserConfigurationException, SAXException, IOException {
        Document document = parse(xml);
        rootTageName = stripNS(document.getFirstChild().getNodeName());
        visitClass(document.getFirstChild());

        List<String> toRemove = new ArrayList<>();
        for (XClass xClass : classes.values()) {
            if (xClass.fields == null || xClass.fields.size() < 1) {
                toRemove.add(xClass.name);
            }
        }

        for (XClass xClass : classes.values()) {
            for (XField field : xClass.fields.values()) {
                if (toRemove.contains(field.dataType)) {
                    field.dataType = "String";
                }
            }
        }

        for (String s : toRemove) {
            classes.remove(s);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(classes));
        return (generateClassText(classes.get(rootTageName)));
    }

    public Document parse(BufferedReader xml) throws IOException, SAXException, ParserConfigurationException {
        String file = "";
        try {
            String str;
            while ((str = xml.readLine()) != null) {
                file += str;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        file = file.replaceAll("<!DOCTYPE((.|\n|\r)*?)\">", "");

        // convert String into InputStream
        InputStream is = new ByteArrayInputStream(file.getBytes());

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setFeature("http://xml.org/sax/features/validation", false);
        dbf.setNamespaceAware(false);
        dbf.setIgnoringComments(true);
        dbf.setValidating(false);
        dbf.setXIncludeAware(true);


       return dbf.newDocumentBuilder().parse(is);
    }

    public String stripNS(String str) {
        if (str.contains(":")) {
            return str.substring(str.indexOf(":") + 1);
        }
        return str;
    }

    public XClass visitClass(Node node) {
        String name = stripNS(node.getNodeName());
        System.out.println("Visiting class:\t" + name);
        if (!classes.containsKey(name)) {
            XClass cla = new XClass();
            cla.name = name;
            classes.put(name, cla);
            System.out.println("\tAdding class:\t" + name);
        }

        XClass cla = classes.get(name);
        System.out.println("\t\tChecking class:\t" + name + " :: " + node.getTextContent());
        if (node.getAttributes() != null && node.getAttributes().getLength() > 0) {
            System.out.println("\t\t\tReading attributes from class " + name);
            for (int i = 0; i < node.getAttributes().getLength(); i++) {
                System.out.println("Gerring attribute #" + i);
                try {
                    String akey = node.getAttributes().item(i).getNodeName();
                    if (akey.contains("xmlns:") || akey.equals("xmlns")) {
                        continue;
                    }
                    akey = stripNS(akey);

                    if (!cla.fields.containsKey(akey)) {
                        XField xf = new XField();
                        xf.name = akey;
                        xf.isInlineList = false;
                        xf.isAttribute = true;
                        System.out.println("\t\t\tAdding attribute field: " + xf.name + " to " + cla.name.toString());
                        cla.fields.put(akey, xf);
                    }
                    XField xf = cla.fields.get(akey);
                    xf.dataType = getLiteralDataType(node.getAttributes().item(i).getNodeValue());
                    cla.fields.put(akey, xf);
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
        } else {
            System.out.println("Has no attributes");
        }


        Map<String, List<Node>> grouped = groupBy(node.getChildNodes(), new Function1<Node, String>() {


            @Override
            public String apply(Node arg) {
                return arg.getNodeName();
            }
        });

        for (Map.Entry<String, List<Node>> entry : grouped.entrySet()) {
            String key = stripNS(entry.getKey());

            System.out.println("\t\t\tLooking at child node: " + key + " of class " + name + ", children: " + entry.getValue().size());
            List<Node> nodes = entry.getValue();
            if (key.equals("#text") && entry.getValue().size() > 1) {
                continue;
            }
            {

                if (!cla.fields.containsKey(key)) {
                    XField field = new XField();
                    field.name = key;
                    cla.fields.put(key, field);
                    System.out.println("Adding field " + cla.name + " -- " + key);
                }

                XField field = cla.fields.get(key);
                if (nodes.size() > 1) {
                    field.isInlineList = true;
                }

                for (Node node1 : nodes) {
                    if (isClass(node1)) {
                        {
                            field.dataType = key;
                            XClass nested = visitClass(node1);

                            if (nested.fields != null && nested.fields.size() == 1 && values(nested.fields).toArray(new XField[1])[0].isInlineList) {
                                classes.remove(nested.name);
                                XField nestedField = values(nested.fields).toArray(new XField[1])[0];
                                field.dataType = nestedField.dataType;
                                field.isList = true;
                            }
                        }
                        if (node1.getAttributes() != null && node1.getAttributes().getLength() > 0) {
                            for (int i = 0; i < node1.getAttributes().getLength(); i++) {
                                System.out.println("Getting attribute #" + i + " of " + node1.getAttributes().getLength());
                                try {
                                    Node n = node.getAttributes().item(i);
                                    if (n == null) continue;
                                    String akey = n.getNodeName();
                                    if (akey.contains("xmlns:") || akey.equals("xmlns")) {
                                        continue;
                                    }
                                    akey = stripNS(akey);

                                    if (!cla.fields.containsKey(akey)) {
                                        XField xf = new XField();
                                        xf.name = akey;
                                        xf.isInlineList = false;
                                        xf.isAttribute = true;
                                        cla.fields.put(akey, xf);
                                        System.out.println("\t\t\tAdding attribute field: " + xf.name + " to " + cla.name.toString());
                                    }
                                    XField xf = cla.fields.get(akey);
                                    xf.dataType = getLiteralDataType(node1.getAttributes().item(i).getNodeValue());
                                    cla.fields.put(akey, xf);
                                } catch (Exception err) {
                                    err.printStackTrace();
                                }
                            }
                        }

                    } else {
                        field.dataType = getLiteralDataType(node1.getNodeValue());
                    }
                }


                cla.fields.put(key, field);
            }
        }

        if (cla.fields == null || cla.fields.size() < 1) {
            System.out.println("Class " + cla.name + " has no fields");
        }

        return cla;
    }

    public String generateClassText(XClass cls) {

        String headers = "", root = "", isStatic = "", fields = "", accessors = "", inners = "";
        if (cls.name.equals(rootTageName)) {

            if (packageName != null) {
                headers = "package " + packageName + ";\n";
            }

            headers += "\nimport org.simpleframework.xml.Attribute;\n" +
                    "import org.simpleframework.xml.Element;\n" +
                    "import org.simpleframework.xml.Text;\n" +
                    "import org.simpleframework.xml.ElementList;\n" +
                    "import org.simpleframework.xml.Root;\n\n" +
                    "import java.net.URL;\n" +
                    "import java.util.List;\n";

            root = "\n@Root(name=\"" + cls.name + "\")\n";

            for (Map.Entry<String, XClass> cl : classes.entrySet()) {
                if (!cl.getValue().name.equals(rootTageName)) {
                    inners += generateClassText(cl.getValue()) + "\n";
                }
            }
        } else {
            indentLevel++;
            root = "\n";
            isStatic = "static ";
        }

        fields = generateFieldText(cls.fields);
        accessors = generateAccessors(cls.fields);

        String indentText = "";

        for (int i = 0; i < indentLevel; i++) {
            indentText += "    ";
        }

        indentLevel--;
        return headers + root + indentText + "public " + isStatic + "class " + mkClassName(cls.name) + " {\n" +
                fields + "" + accessors + inners + "\n" + indentText + "}";
    }

    public String generateFieldText(HashMap<String, XField> fields) {
        String str = "";
        indentLevel++;
        for (Map.Entry<String, XField> field : fields.entrySet()) {
            XField f = field.getValue();

            if (f.name.equals("#text")) {
                f.name = "textValue";
                f.dataType = "String";
                f.isInlineList = false;
                f.isList = false;
            }

            String annotation = f.isAttribute ? "@Attribute(name=\"" + f.name + "\", required=false)" : "@Element(name=\"" + f.name + "\", required=false)";
            boolean isClass = classes.containsKey(f.dataType);
            String dataType = isClass ? mkClassName(f.dataType) : f.dataType;

            if (f.isList || f.isInlineList) {
                dataType = "List<" + dataType + ">";

                annotation = "@ElementList(name=\"" + f.name + "\", required=false" + (f.isInlineList ? ", entry=\"" + f.name + "\", inline=true)" : ")");

            }

            if (f.name.equals("textValue")) {
                annotation = "@Text(required=false)";
            }

            String indentText = "";

            for (int i = 0; i < indentLevel; i++) {
                indentText += "    ";
            }


            str += "\n" + indentText + annotation + "\n" + indentText + dataType + " " + mkFieldName(f.name) + ";\n";
        }
        indentLevel--;
        return str;
    }

    public String generateAccessors(HashMap<String, XField> fields) {
        String str = "";
        indentLevel++;

        String indentText = "";

        for (int i = 0; i < indentLevel; i++) {
            indentText += "    ";
        }


        for (Map.Entry<String, XField> field : fields.entrySet()) {
            XField f = field.getValue();
            boolean isClass = classes.containsKey(f.dataType);
            String dataType = isClass ? mkClassName(f.dataType) : f.dataType;
            if (f.isList || f.isInlineList) {
                dataType = "List<" + dataType + ">";
            }

            str += "\n" + indentText + "public " + dataType + " get" + cap(mkFieldName(cap(f.name))) + "() {return this." + mkFieldName(f.name) + ";}\n";
            str += indentText + "public void set" + cap(mkFieldName(cap(f.name))) + "(" + dataType + " value) {this." + mkFieldName(f.name) + " = value;}\n";
        }
        indentLevel--;
        return str;
    }

    public String mkFieldName(String name) {
        name = getLastDotInList(name);
        name = name.replace("-", "_");
        name = name.replace("#", "");
        name = name.substring(0, 1).toLowerCase() + name.substring(1);
        if (name.endsWith("_")) {
            name = name.substring(0, name.length() - 1);
        }
        while (name.contains("_")) {
            int pos = name.indexOf("_");
            name = name.substring(0, pos) + name.substring(pos + 1, pos + 2).toUpperCase() + name.substring(pos + 2);
        }
        return reservedCheck(name);
    }

    private String getLiteralDataType(String nodeValue) {
        if (nodeValue == null) return "String";
        try {
            DateType.getDate(nodeValue);
            return "Date";
        } catch (Exception ignored) {

        }
        try {
            new URL(nodeValue);
            return "URL";
        } catch (Exception ignored) {

        }
        if (nodeValue.equals("true") || nodeValue.equals("false")) {
            return "Boolean";
        } else if (isNumeric(nodeValue)) {
            if (nodeValue.contains(".")) {
                return "Double";
            } else {
                try {
                    //noinspection ResultOfMethodCallIgnored
                    Integer.parseInt(nodeValue);
                    return "Integer";
                } catch (NumberFormatException err) {
                    return "Long";
                }
            }
        }

        return "String";
    }

    public boolean isNumeric(String value) {
        if (value.contains(" ")) {
            return false;
        }
        try {
            NumberFormat.getInstance().parse(value);
            return true;
        } catch (ParseException e) {
            // Not a number.
            return false;
        }
    }

    public boolean isClass(Node node) {
        int childNodes = 0;
        int attributes = 0;
        boolean isTextOnly = false;

        if (node.getChildNodes() != null && node.getChildNodes().getLength() > 0) {
            childNodes = node.getChildNodes().getLength();
        }

        if (node.getAttributes() != null && node.getAttributes().getLength() > 0) {
            attributes = node.getAttributes().getLength();
        }

        if (node.getChildNodes() != null && node.getChildNodes().getLength() == 1 && node.getChildNodes().item(0).getNodeName().equals("#text") && attributes == 0) {
            isTextOnly = true;
        }

        return !isTextOnly;
    }


}
