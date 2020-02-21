<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    >

    <xsl:output  method="xml" encoding="UTF-8" omit-xml-declaration="no"/>

    <xsl:strip-space elements="*"/>

    <xsl:template match="*">
        <xsl:element name="{local-name()}" namespace="{namespace-uri()}">
            <xsl:apply-templates select="@* | node()"/>
        </xsl:element>
    </xsl:template>
          
    <xsl:template match="@*">
        <xsl:attribute name="{local-name()}" namespace="{namespace-uri()}">
            <xsl:value-of select="."/>
        </xsl:attribute>
    </xsl:template>

   <xsl:template match="@*[local-name()='lang']">
        <xsl:attribute name="{local-name()}" >
            <xsl:value-of select="."/>
        </xsl:attribute>
    </xsl:template>

    <xsl:template match="text()">
        <xsl:value-of select="normalize-space(.)"/>
    </xsl:template>

    <xsl:template match="//*[local-name()='relatedIdentifier']">
        <xsl:element name="{local-name()}" namespace="{namespace-uri()}">
            <xsl:value-of select="normalize-space(@relationType)"/>
            <xsl:text>:</xsl:text>
            <xsl:value-of select="normalize-space(@relatedIdentifierType)"/>
            <xsl:text>:</xsl:text>
            <xsl:value-of select="normalize-space(.)"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="//*[local-name()='alternateIdentifier']">
        <xsl:element name="{local-name()}" namespace="{namespace-uri()}">
            <xsl:value-of select="normalize-space(@alternateIdentifierType)"/>
            <xsl:text>:</xsl:text>
            <xsl:value-of select="normalize-space(.)"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="//*[local-name()='nameIdentifier']">
        <xsl:element name="{local-name()}" namespace="{namespace-uri()}">
            <xsl:value-of select="normalize-space(@nameIdentifierScheme)"/>
            <xsl:text>:</xsl:text>
            <xsl:value-of select="normalize-space(.)"/>
        </xsl:element>
    </xsl:template>

<!--lon lat-->
    <xsl:template match="//*[local-name()='geoLocationPoint']/*[local-name()='pointLatitude']">
        <xsl:element name="geo" namespace="{namespace-uri()}">
            <xsl:value-of select="normalize-space(../*[local-name()='pointLongitude'])"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="normalize-space(../*[local-name()='pointLatitude'])"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="//*[local-name()='geoLocationPoint' and starts-with(namespace-uri(.),'http://datacite.org/schema/kernel-3')]">
        <xsl:element name="geo" namespace="{namespace-uri()}">
            <xsl:value-of select="substring-after(.,' ')"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="substring-before(.,' ')"/>
        </xsl:element>
    </xsl:template>

<!--minlon minlat maxlon maxlat-->
    <xsl:template match="//*[local-name()='geoLocationBox']/*[local-name()='westBoundLongitude']">
        <xsl:element name="geo" namespace="{namespace-uri()}">
            <xsl:value-of select="normalize-space(../*[local-name()='westBoundLongitude'])"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="normalize-space(../*[local-name()='southBoundLatitude'])"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="normalize-space(../*[local-name()='eastBoundLongitude'])"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="normalize-space(../*[local-name()='northBoundLatitude'])"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="//*[local-name()='geoLocationBox' and starts-with(namespace-uri(.),'http://datacite.org/schema/kernel-3')]">
        <xsl:element name="geo" namespace="{namespace-uri()}">
            <xsl:value-of select="substring-before(substring-after(.,' '),' ')"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="substring-before(.,' ')"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="substring-after(substring-after(substring-after(.,' '),' '),' ')"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="substring-before(substring-after(substring-after(.,' '),' '),' ')"/>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
