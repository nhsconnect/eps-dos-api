<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="text"/>
    <xsl:template match="/">
        {<xsl:apply-templates select="@*|node()"/>}
    </xsl:template>
    
    <xsl:template match="Dispenser">
        "ods":"<xsl:value-of select="@organisationCode"/>",
        "name":<xsl:value-of select="@name"/>",
        "address":{
            "line":["<xsl:value-of select="@street"/>", "<xsl:value-of select="@locality"/>", "<xsl:value-of select="@Town"/>", "<xsl:value-of select="@administrative"/>"],
            "postcode":"<xsl:value-of select="@postcode"/>" 
        }
    </xsl:template>
    
    <xsl:template match="@*|node()">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
</xsl:stylesheet>
