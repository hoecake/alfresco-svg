<?xml version="1.0" encoding="utf-8"?>
<!--
     This is a probably very badly written and
     incorrect stylesheet to convert SVG into
     text. Please make suggestions for improvement.

     It is meant to produce non-verbose output, otherwise
     a simple empty stylesheet would have done it (with
     lots of spaces).

     The disadvantage of this is that it ignores elements 
     in other namespaces, only outputting the text within
     the children of the text, desc, title and metadata
     elements.

     Author: Dean Jackson <dean@w3.org>
-->

<xsl:stylesheet 
  xmlns:svg="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0">
  
  <xsl:output method="text" indent="no"/>
  
  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="svg:text">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="svg:text//text()|svg:desc//text()|svg:title//text()|svg:metadata//text()">
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text> 
</xsl:text>
  </xsl:template>

  <xsl:template match="text()">
  </xsl:template>

</xsl:stylesheet>
