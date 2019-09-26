# CHANGES TO VALG.NO CODE

- EVA Admin 2017 was git sha a7435cd0be89d03b4940eaf80792eb70dac24f4b "admin-2019.22.7829"
- This was downloaded 08.06.2019 from:
https://www.valg.no/valg-i-norge/valggjennomforing-i-norge/elektronisk-valgadministrasjonssystem/systemdokumentasjon-og-kildekode-i-eva

- EVA Admin 2019 was git sha fd7009b544eecb4ac7789d3be08d985e86bbcc91 "admin-2019.22.7829"
- This was downloaded 08.06.2019 from:
https://www.valg.no/valg-i-norge/valggjennomforing-i-norge/elektronisk-valgadministrasjonssystem/systemdokumentasjon-og-kildekode-i-eva
- Looks like no change at all basically. Adds jackson-jaxrs-json-provider as dependency.

## Findings
- 'admin-docker' missing
- 'admin-testing' missing
- 'admin-other' missing

# EVA Admin

Dette er kildekoden til EVA Admin - applikasjonen som er "navet" i EVA.
Dokumentasjon om EVA finner du i admin-teamets Wiki, Confluence. 
Følgende sider er gode inngangsporter:

* [EVA Admin-applikasjonens hjemmeside](https://confluence.valg.no/display/EA/EVA+Admin)
* [Ny i prosjektet](https://confluence.valg.no/display/FELLES/Ny+i+prosjektet) (inkludert info om hvordan sette opp EVA Admin)
* [EVA Admin arkitektur](https://confluence.valg.no/display/EA/Arkitektur+EVA+Admin)
* [Organisering av kodebasen](https://confluence.valg.no/display/EA/Kodeorganisering)

# Inndeling av kodebasen

Kodebasen er delt opp i 3 hoveddeler:

* admin - selve admin-applikasjonen
* MISSING FROM RELEASE CODE - admin-docker - docker-oppsett for å enkelt kunne kjøre EVA Admin
* MISSING FROM RELEASE CODE - admin-testing - integrasjonstester og funksjonelle tester
* MISSING FROM RELEASE CODE - admin-other - verktøy og andre ting som ikke faller innenfor de to første kategoriene

# Lisens

Kildekoden til EVA Admin er lisensbelagt. Se lisens.md for mer informasjon

# Bygging

EVA Admin bygges med Maven, etter standard konvensjoner.

MISSING FROM RELEASE CODE - Merk at Docker-oppsettet håndterer bygging automatisk dersom man bruker dette.

## Organisering av Maven-konfigurasjon

Maven-konfigurasjonen er organisert etter følgende prinsipper:

* Overordnet clean code-prinsipp: _Hvis noe fremstår som mystisk - legg inn en forklaring på hvorfor det er slik det er_
* Versjonsnumre samles i rot-pom-fila
* Alle under-pom-filer refererer bare til artefaktene (og ikke versjonsnumre)
* I rot-pom-fila dokumenteres hva hvert bibliotek gjør (i dependencyManagement-delen), samt evt. hva den brukes til, hvis litt utenom det vanlige
* Hvis man kjører `mvn dependency:analyze` får man ut en liste over avhengigheter som ikke er som forventet:
  * Det skal alltid være forklart hvorfor en avhengighet som fremstår som ubrukt er med
  * Avhengigheter som er brukt, men ikke deklarert bør deklareres (de bruker som regel en annen transitiv avhengighet, og dette bør unngås)
* Avhengighetene grupperes etter:
  * Interne (refererer til `no.valg.eva`-kode)
  * Eksterne (refererer til kode andre har laget)
* Avhengighetene sorteres alfabetisk
* Avhengighetene skal bruke siste tilgjengelige versjon. Dersom ikke siste versjon kan brukes, skal det eksplisitt forklares hvorfor

## Kjøre Sonar plugin lokalt
For å kjøre Maven Sonar plugin lokalt med definisjoner fra sonar.eva.lokal:

`$ mvn clean install sonar:sonar -Dsonar.host.url=http://sonar.eva.lokal`