package svg.transform;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.AbstractContentTransformer2;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

public class SvgResizeTransformer extends AbstractContentTransformer2 {
	private static final Logger LOG = Logger.getLogger(SvgTransformer.class);
//	private static final String SVG2TEXT_XSL = "META-INF/resources/svg/svg2text.xsl";
//
//	private final DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
//	private final TransformerFactory tFactory = TransformerFactory.newInstance();
//	private final Transformer transformer; 
    
    public SvgResizeTransformer() throws Exception {
//    	if (LOG.isDebugEnabled()) LOG.debug("INIT - " + SVG2TEXT_XSL);
//    	dfactory.setNamespaceAware(true);
//    	InputStream in = SvgTransformer.class.getClassLoader().getResourceAsStream(SVG2TEXT_XSL);
//    	try {
//	    	if (null == in) {
//	    		LOG.error("in is null! " + SVG2TEXT_XSL + " not found", new Exception(SVG2TEXT_XSL));
//	    	}
//	    	if (LOG.isDebugEnabled()) {
//	    		LOG.debug(IOUtils.toString(in));
//	    		in = SvgTransformer.class.getClassLoader().getResourceAsStream(SVG2TEXT_XSL);
//	    	}
//	    	 if (LOG.isTraceEnabled()) LOG.trace("Resource: " + in);
//	    	transformer = tFactory.newTransformer(new StreamSource(in));
//    	} finally {
//    		IOUtils.closeQuietly(in);
//    	}
//    	if (LOG.isTraceEnabled()) LOG.trace("COMPLETED");
    }
    
  @Override
  public boolean isTransformable(String sourceMimetype,
      String targetMimetype, TransformationOptions options) {
	  
	  if (LOG.isDebugEnabled()) LOG.debug("isTransformable form: " + sourceMimetype + " to " + targetMimetype);
    if (MimetypeMap.MIMETYPE_IMAGE_SVG.equalsIgnoreCase(sourceMimetype)
     
    		&& MimetypeMap.MIMETYPE_IMAGE_SVG.equalsIgnoreCase(targetMimetype)) {
    	if (LOG.isTraceEnabled()) LOG.trace("TRUE");
      return true;
    }
    if (LOG.isTraceEnabled()) LOG.trace("FALSE");
    return false;
  }

  
  
	@Override
	protected void transformInternal(ContentReader reader,
			ContentWriter writer, TransformationOptions options)
			throws Exception {
		if (!(options instanceof ImageTransformationOptions)) {
			LOG.error("cannot transform using none image options");
		}
		ImageTransformationOptions iOptions = (ImageTransformationOptions) options;
		String svg = reader.getContentString();
		int index = null != svg ? svg.indexOf("<svg"):-1;
		final String svgString;
		if (null == svg) {
			svgString = "";
			
		} else if (1 > index) {
			svgString = svg;
		} else {
			svgString = svg.substring(index);
		}
		//0 0 500 400
		final StringBuilder sb = new StringBuilder("<svg width=\"") 
			.append(iOptions.getResizeOptions().getWidth())
			.append("\" height=\"")
				.append(iOptions.getResizeOptions().getHeight())
				.append("\" viewBox=\"0 0 500 500\" ")
				.append(" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\">")
				.append(" preserveAspectRatio=\"xMidYMid meet\"")
				.append("<g id=\"g1\">")
				.append(svgString)
				.append("</g>")
				.append("<script type=\"application/ecmascript\"> ")
				.append("var width=")
				.append(iOptions.getResizeOptions().getWidth())
				.append(", height=")
				.append(iOptions.getResizeOptions().getHeight())
				.append(";var node = document.getElementById(\"g1\");")
				.append("var bb = node.getBBox();")
				.append("var matrix = \"matrix(\"+width / bb.width+\", 0, 0, \"+height / bb.height+\", 0,0)\";")
				.append("node.setAttribute(\"transform\", matrix);")
				.append("</script></svg>");
		if (LOG.isDebugEnabled())
			LOG.debug("TRANSFORM " + options);
		OutputStream out = null;
		try{
			out = new BufferedOutputStream(
				writer.getContentOutputStream());

		out.write(sb.toString().getBytes());
		} catch (Exception e) {
			LOG.error("Error transforming SVG", e);
		} finally {
			IOUtils.closeQuietly(out);
		}
		
	}
	
	
  public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = tf.newTransformer();
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

	    transformer.transform(new DOMSource(doc), 
	         new StreamResult(new OutputStreamWriter(out, "UTF-8")));
	}

}