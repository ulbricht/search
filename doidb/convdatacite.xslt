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
    <xsl:template match="//*[local-name()='geoLocationPoint']">
        <xsl:variable name="lon">
            <xsl:choose>
                <xsl:when test="starts-with(namespace-uri(.),'http://datacite.org/schema/kernel-3')">
                    <xsl:value-of select="normalize-space(substring-after(.,' '))"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="normalize-space(*[local-name()='pointLongitude'])"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="lat">
            <xsl:choose>
                <xsl:when test="starts-with(namespace-uri(.),'http://datacite.org/schema/kernel-3')">
                    <xsl:value-of select="normalize-space(substring-before(.,' '))"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="normalize-space(*[local-name()='pointLatitude'])"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$lat='' or $lon=''"/>            
            <xsl:otherwise>
                <xsl:element name="geo" namespace="{namespace-uri()}">
                    <xsl:text>POINT(</xsl:text>
                    <xsl:value-of select="$lon"/>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="$lat"/>
                    <xsl:text>)</xsl:text>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

<!--minlon minlat maxlon maxlat-->
    <xsl:template match="//*[local-name()='geoLocationBox']">
        <xsl:variable name="minlon">
            <xsl:choose>
                <xsl:when test="starts-with(namespace-uri(.),'http://datacite.org/schema/kernel-3')">
                    <xsl:value-of select="normalize-space(substring-before(substring-after(.,' '),' '))"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="normalize-space(*[local-name()='westBoundLongitude'])"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="minlat">
            <xsl:choose>
                <xsl:when test="starts-with(namespace-uri(.),'http://datacite.org/schema/kernel-3')">
                    <xsl:value-of select="normalize-space(substring-before(.,' '))"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="normalize-space(*[local-name()='southBoundLatitude'])"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="maxlon">
            <xsl:choose>
                <xsl:when test="starts-with(namespace-uri(.),'http://datacite.org/schema/kernel-3')">
                    <xsl:value-of select="normalize-space(substring-after(substring-after(substring-after(.,' '),' '),' '))"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of  select="normalize-space(*[local-name()='eastBoundLongitude'])"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="maxlat">
            <xsl:choose>
                <xsl:when test="starts-with(namespace-uri(.),'http://datacite.org/schema/kernel-3')">
                    <xsl:value-of select="normalize-space(substring-before(substring-after(substring-after(.,' '),' '),' '))"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="normalize-space(*[local-name()='northBoundLatitude'])"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>      
        <xsl:choose>
            <xsl:when test="$minlat &gt; $maxlat"/>
            <xsl:when test="$minlon &gt; $maxlon"/>
            <xsl:when test="$minlon='' or $maxlon='' or $minlat='' or $maxlat=''"/>
            <xsl:when test="$minlon=$maxlon and $minlat=$maxlat">
                <xsl:element name="geo" namespace="{namespace-uri()}">
                    <xsl:text>POINT(</xsl:text>
                    <xsl:value-of select="$minlon"/>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="$minlat"/>
                    <xsl:text>)</xsl:text>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="geo" namespace="{namespace-uri()}">
                    <xsl:text>POLYGON((</xsl:text>
                    <xsl:value-of select="$minlon"/>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="$minlat"/>
                    <xsl:text>, </xsl:text>
                    <xsl:value-of select="$maxlon"/>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="$minlat"/>
                    <xsl:text>, </xsl:text>
                    <xsl:value-of select="$maxlon"/>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="$maxlat"/>
                    <xsl:text>, </xsl:text>
                    <xsl:value-of select="$minlon"/>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="$maxlat"/>
                    <xsl:text>, </xsl:text>
                    <xsl:value-of select="$minlon"/>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="$minlat"/>
                    <xsl:text>))</xsl:text>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
