param(
    [string]$OutputPath = [System.IO.Path]::Combine([Environment]::GetFolderPath('DesktopDirectory'), 'user-import-test-template.xlsx')
)

$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

function Escape-XmlText {
    param([string]$Text)
    if ($null -eq $Text) { return '' }
    return [System.Security.SecurityElement]::Escape($Text)
}

function From-CodePoints {
    param([int[]]$CodePoints)
    $chars = foreach ($codePoint in $CodePoints) { [char]$codePoint }
    return -join $chars
}

function Convert-ToCellRef {
    param(
        [int]$ColumnIndex,
        [int]$RowIndex
    )

    $n = $ColumnIndex + 1
    $letters = ''
    while ($n -gt 0) {
        $mod = ($n - 1) % 26
        $letters = [char](65 + $mod) + $letters
        $n = [math]::Floor(($n - 1) / 26)
    }
    return "$letters$RowIndex"
}

function Convert-RowToXml {
    param(
        [string[]]$Values,
        [int]$RowIndex
    )

    $cells = New-Object System.Collections.Generic.List[string]
    for ($i = 0; $i -lt $Values.Count; $i++) {
        $value = $Values[$i]
        if ([string]::IsNullOrEmpty($value)) {
            continue
        }
        $ref = Convert-ToCellRef -ColumnIndex $i -RowIndex $RowIndex
        $escaped = Escape-XmlText $value
        $cells.Add("<c r=""$ref"" t=""inlineStr""><is><t xml:space=""preserve"">$escaped</t></is></c>")
    }
    return "<row r=""$RowIndex"">$(($cells -join ''))</row>"
}

function New-ZipEntry {
    param(
        [System.IO.Compression.ZipArchive]$Archive,
        [string]$EntryPath,
        [string]$Content
    )

    $entry = $Archive.CreateEntry($EntryPath)
    $stream = $entry.Open()
    try {
        $writer = New-Object System.IO.StreamWriter($stream, [System.Text.Encoding]::UTF8)
        try {
            $writer.Write($Content)
        }
        finally {
            $writer.Dispose()
        }
    }
    finally {
        $stream.Dispose()
    }
}

$deptEdu = From-CodePoints 0x6559,0x80B2,0x5B66,0x9662
$majorEdu = From-CodePoints 0x6559,0x80B2,0x5B66
$grade2023 = '2023' + (From-CodePoints 0x7EA7)
$studentName = From-CodePoints 0x6D4B,0x8BD5,0x5B66,0x751F,0x4E00
$teacherName = From-CodePoints 0x6D4B,0x8BD5,0x8001,0x5E08,0x4E8C
$adminName = From-CodePoints 0x6D4B,0x8BD5,0x7BA1,0x7406,0x5458,0x4E09

$headers = @('Name','Identity Type','Student No','Teacher No','Phone','Email','Dept Code/Name','Major Code/Name','Grade Code/Name','Status','Role Codes','Password')
$rows = @(
    @($studentName,'STUDENT','20260011','','13890000011','stu11@example.com',$deptEdu,$majorEdu,$grade2023,'ENABLED','STUDENT','123456'),
    @($teacherName,'TEACHER','','T90011','13890000012','tea12@example.com','EDU','','','ENABLED','INNER_TEACHER','123456'),
    @($adminName,'ADMIN','','A90011','13890000013','adm13@example.com',$deptEdu,'','','ENABLED','SYS_ADMIN','123456')
)

$sheet1Rows = New-Object System.Collections.Generic.List[string]
$sheet1Rows.Add((Convert-RowToXml -Values $headers -RowIndex 1))
for ($i = 0; $i -lt $rows.Count; $i++) {
    $sheet1Rows.Add((Convert-RowToXml -Values $rows[$i] -RowIndex ($i + 2)))
}

$sheet1Xml = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheetData>
$($sheet1Rows -join "`r`n")
  </sheetData>
</worksheet>
"@

$noteLines = @(
    'This workbook is for testing bulk user import.',
    'Dept / Major / Grade support code or name.',
    'Example: EDU / Education College, JY001 / Education, G2023 / 2023 Grade.',
    'If one row fails, the other rows should still be processed.'
)

$sheet2Rows = New-Object System.Collections.Generic.List[string]
for ($i = 0; $i -lt $noteLines.Count; $i++) {
    $sheet2Rows.Add((Convert-RowToXml -Values @($noteLines[$i]) -RowIndex ($i + 1)))
}

$sheet2Xml = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheetData>
$($sheet2Rows -join "`r`n")
  </sheetData>
</worksheet>
"@

$workbookXml = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Import Template" sheetId="1" r:id="rId1"/>
    <sheet name="Notes" sheetId="2" r:id="rId2"/>
  </sheets>
</workbook>
"@

$workbookRelsXml = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>
"@

$rootRelsXml = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>
"@

$contentTypesXml = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
  <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
  <Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
</Types>
"@

$stylesXml = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <fonts count="1">
    <font>
      <sz val="11"/>
      <color theme="1"/>
      <name val="Arial"/>
      <family val="2"/>
    </font>
  </fonts>
  <fills count="1">
    <fill>
      <patternFill patternType="none"/>
    </fill>
  </fills>
  <borders count="1">
    <border>
      <left/><right/><top/><bottom/><diagonal/>
    </border>
  </borders>
  <cellStyleXfs count="1">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
  </cellStyleXfs>
  <cellXfs count="1">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
  </cellXfs>
</styleSheet>
"@

$created = [System.DateTime]::UtcNow.ToString("yyyy-MM-ddTHH:mm:ssZ")
$coreXml = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:dcmitype="http://purl.org/dc/dcmitype/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <dc:creator>Codex</dc:creator>
  <cp:lastModifiedBy>Codex</cp:lastModifiedBy>
  <dcterms:created xsi:type="dcterms:W3CDTF">$created</dcterms:created>
  <dcterms:modified xsi:type="dcterms:W3CDTF">$created</dcterms:modified>
</cp:coreProperties>
"@

$appXml = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
  <Application>Microsoft Excel</Application>
  <DocSecurity>0</DocSecurity>
  <ScaleCrop>false</ScaleCrop>
  <HeadingPairs>
    <vt:vector size="2" baseType="variant">
      <vt:variant><vt:lpstr>Worksheets</vt:lpstr></vt:variant>
      <vt:variant><vt:i4>2</vt:i4></vt:variant>
    </vt:vector>
  </HeadingPairs>
  <TitlesOfParts>
    <vt:vector size="2" baseType="lpstr">
      <vt:lpstr>Import Template</vt:lpstr>
      <vt:lpstr>Notes</vt:lpstr>
    </vt:vector>
  </TitlesOfParts>
  <Company></Company>
  <LinksUpToDate>false</LinksUpToDate>
  <SharedDoc>false</SharedDoc>
  <HyperlinksChanged>false</HyperlinksChanged>
  <AppVersion>16.0300</AppVersion>
</Properties>
"@

if (Test-Path $OutputPath) {
    Remove-Item -LiteralPath $OutputPath -Force
}

$fileStream = [System.IO.File]::Open($OutputPath, [System.IO.FileMode]::CreateNew)
try {
    $archive = New-Object System.IO.Compression.ZipArchive($fileStream, [System.IO.Compression.ZipArchiveMode]::Create, $false)
    try {
        New-ZipEntry -Archive $archive -EntryPath '[Content_Types].xml' -Content $contentTypesXml
        New-ZipEntry -Archive $archive -EntryPath '_rels/.rels' -Content $rootRelsXml
        New-ZipEntry -Archive $archive -EntryPath 'docProps/core.xml' -Content $coreXml
        New-ZipEntry -Archive $archive -EntryPath 'docProps/app.xml' -Content $appXml
        New-ZipEntry -Archive $archive -EntryPath 'xl/workbook.xml' -Content $workbookXml
        New-ZipEntry -Archive $archive -EntryPath 'xl/_rels/workbook.xml.rels' -Content $workbookRelsXml
        New-ZipEntry -Archive $archive -EntryPath 'xl/styles.xml' -Content $stylesXml
        New-ZipEntry -Archive $archive -EntryPath 'xl/worksheets/sheet1.xml' -Content $sheet1Xml
        New-ZipEntry -Archive $archive -EntryPath 'xl/worksheets/sheet2.xml' -Content $sheet2Xml
    }
    finally {
        $archive.Dispose()
    }
}
finally {
    $fileStream.Dispose()
}

Write-Host $OutputPath
