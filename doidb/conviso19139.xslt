<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output  method="xml" encoding="utf-8" omit-xml-declaration="no"/>

    <xsl:strip-space elements="*"/>

    <xsl:template match="*">
        <xsl:element name="MD_Metadata" namespace="{namespace-uri()}">
          <xsl:apply-templates select="//*[local-name()='MD_Metadata']//*[local-name()='MD_Keywords']/*[local-name()='keyword']/*[local-name()='CharacterString' and normalize-space(../../*[local-name()='thesaurusName']//*[local-name()='title'])='NASA/GCMD Earth Science Keywords']"/>
        </xsl:element>
    </xsl:template>
         
    <xsl:template match="//*[local-name()='MD_Metadata']//*[local-name()='MD_Keywords']/*[local-name()='keyword']/*[local-name()='CharacterString' and normalize-space(../../*[local-name()='thesaurusName']//*[local-name()='title'])='NASA/GCMD Earth Science Keywords']">
        <xsl:call-template name="listtokenized">
            <xsl:with-param name="keyword" select="." />            
            <xsl:with-param name="path" select="''" /> 
        </xsl:call-template>
    </xsl:template>


    <xsl:template name="listtokenized">
        <xsl:param name="keyword" />
        <xsl:param name="path" /> 

        <xsl:variable name="token" >
            <xsl:choose>
                <xsl:when test="contains($keyword,'&gt;')">
                    <xsl:value-of select="normalize-space(substring-before($keyword,'&gt;'))" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="normalize-space($keyword)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="remain" select="normalize-space(substring-after($keyword,'&gt;'))" />

        <xsl:variable name="newpath" >
            <xsl:choose>
                <xsl:when test="$path != ''">
                    <xsl:value-of select="normalize-space($path)"/>
                    <xsl:text> &gt; </xsl:text>
                    <xsl:value-of select="$token"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$token"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:element name="gcmdkeywords" namespace="{namespace-uri()}">
            <xsl:value-of select="$newpath"/>
        </xsl:element>

        <xsl:if test="normalize-space($remain) != ''">
            <xsl:call-template name="listtokenized">
                <xsl:with-param name="keyword" select="$remain"/> 
                <xsl:with-param name="path" select="$newpath" /> 
            </xsl:call-template>
        </xsl:if>

    </xsl:template>


</xsl:stylesheet>
