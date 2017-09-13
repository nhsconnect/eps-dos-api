<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns="http://www.nhs.uk/nhswebservices/" >
    <xsl:output method="xml"/>
    <xsl:template match="/string"><xsl:value-of select="."/></xsl:template>
</xsl:stylesheet>
