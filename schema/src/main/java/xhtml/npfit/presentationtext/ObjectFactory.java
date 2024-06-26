
package xhtml.npfit.presentationtext;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the xhtml.npfit.presentationtext package. 
 * &lt;p&gt;An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _PBr_QNAME = new QName("xhtml:NPfIT:PresentationText", "br");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: xhtml.npfit.presentationtext
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link H2 }
     * 
     */
    public H2 createH2() {
        return new H2();
    }

    /**
     * Create an instance of {@link H3 }
     * 
     */
    public H3 createH3() {
        return new H3();
    }

    /**
     * Create an instance of {@link H4 }
     * 
     */
    public H4 createH4() {
        return new H4();
    }

    /**
     * Create an instance of {@link H5 }
     * 
     */
    public H5 createH5() {
        return new H5();
    }

    /**
     * Create an instance of {@link H6 }
     * 
     */
    public H6 createH6() {
        return new H6();
    }

    /**
     * Create an instance of {@link P }
     * 
     */
    public P createP() {
        return new P();
    }

    /**
     * Create an instance of {@link Ol }
     * 
     */
    public Ol createOl() {
        return new Ol();
    }

    /**
     * Create an instance of {@link Li }
     * 
     */
    public Li createLi() {
        return new Li();
    }

    /**
     * Create an instance of {@link A }
     * 
     */
    public A createA() {
        return new A();
    }

    /**
     * Create an instance of {@link Ul }
     * 
     */
    public Ul createUl() {
        return new Ul();
    }

    /**
     * Create an instance of {@link Table }
     * 
     */
    public Table createTable() {
        return new Table();
    }

    /**
     * Create an instance of {@link Caption }
     * 
     */
    public Caption createCaption() {
        return new Caption();
    }

    /**
     * Create an instance of {@link Thead }
     * 
     */
    public Thead createThead() {
        return new Thead();
    }

    /**
     * Create an instance of {@link Tr }
     * 
     */
    public Tr createTr() {
        return new Tr();
    }

    /**
     * Create an instance of {@link Th }
     * 
     */
    public Th createTh() {
        return new Th();
    }

    /**
     * Create an instance of {@link Td }
     * 
     */
    public Td createTd() {
        return new Td();
    }

    /**
     * Create an instance of {@link Tfoot }
     * 
     */
    public Tfoot createTfoot() {
        return new Tfoot();
    }

    /**
     * Create an instance of {@link Tbody }
     * 
     */
    public Tbody createTbody() {
        return new Tbody();
    }

    /**
     * Create an instance of {@link Pre }
     * 
     */
    public Pre createPre() {
        return new Pre();
    }

    /**
     * Create an instance of {@link BrType }
     * 
     */
    public BrType createBrType() {
        return new BrType();
    }

    /**
     * Create an instance of {@link HeadType }
     * 
     */
    public HeadType createHeadType() {
        return new HeadType();
    }

    /**
     * Create an instance of {@link BodyType }
     * 
     */
    public BodyType createBodyType() {
        return new BodyType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BrType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link BrType }{@code >}
     */
    @XmlElementDecl(namespace = "xhtml:NPfIT:PresentationText", name = "br", scope = P.class)
    public JAXBElement<BrType> createPBr(BrType value) {
        return new JAXBElement<>(_PBr_QNAME, BrType.class, P.class, value);
    }

}
