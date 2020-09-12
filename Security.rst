Sicherheit der App
==================


Bei Nicht-Verwendung des lokalen Loginspeichers ist die Verwendung dieser App
sicherheitstechnisch äquivalent zur Verwendung der Webseite im Browser oder
einer hypothetischen offiziellen und sauber implementierten AoD-App.

Anstelle des appinternen Loginspeichers  kann auch einfach AoD's „Login merken“
oder ein Passwortmanager, wie zB KeypassDX_, verwendet werden.

Mit Verwendung des lokalen Loginspeichers erhöht sich die Angriffsfläche zwar
etwas, aber solange keine gezielten Angriffe auf euer Gerät zur Erlangung der
AoD-Daten, die entweder root-Rechte erlangen konnten oder physischen Zugriff
auf das Gerät haben *(letzteres nur bei fehlender Speicherverschlüsselung
zutreffend)*, zu erwarten sind, ist auch hier das Risiko unbedenklich.
*(Natürlich verwendest du für AoD nicht dieselben Zugangsdaten wie für deine
Finanz- und Mailkonten, richtig?)*


Langversion:
------------

Das Passwort wird in Androids ``SharedPreferences`` der App im *privaten* Modus
gespeichert (``Context.MODE_PRIVATE``).
Das bedeutetet es wird in einer XML-Datei im App-eigenen privaten data-Ordner
abgespeichert; in unserem Falle also ``/data/data/de.oneric.aid/shared_prefs``.
Andere nicht-privilegierte Apps können die dort abgelegten Daten nicht lesen
oder verändern und auch ""normaler"" Benutzer ohne Root-Rechte wird der Zugriff
darauf ebenfalls verwehrt (anders als bei ``/sdcard/Android/data/…``).
Potentielle nicht-privilegierte Hintertüren um die Datei doch lesen zu können,
wären Verschieben auf eine SD-Karte und anschließendes Auslesen am PC, oder die
Verwendung der Backup-Funktion. Aus diesem Gründen ist das Verschieben auf die
SD-Karte sowie das Data-Backup für diese App in der Konfiguration unterbunden.

Nun könnten wir die Möglichkeit von Sicherheitslücken, durch die Malware
root-Rechte erlangen könnte einfach ignorieren und hätten ein "sicher"
hinterlegtes Passwort. In Anbetracht des Umstandes, dass mobile Geräte ein sehr
interessantes Ziel für Malware sind, die Verfügbarkeit und/oder Installation
von Sicherheitsaktualisierungen im Android-Ökosystem eher mau sind, sowie das
viele Nutzer mit den „Google-Play-Services“ eine anfällige, proprietäre mit
root-Rechten versehene Software auf den Geräten laufen haben, hielte ich das
aber für eine schlechte Entscheidung.

Daher speichern wir das Passwort nicht einfach im Klartext, sondern
*verschleiern* es zuvor.
Technisch gesehen handelt es sich um eine Verschlüsselung.
Zusätzlich wird die Länge normalisiert, so dass mindestens 32 Byte gespeichert
werden und die Byte-Länge immer eine Zweierpotenz ist, um zu verhindern, dass
möglicherweise nützliche Information über eure Passwortlänge nur aus den
SharedPreferences abgelesen werden kann [#length]_ .
Ist das Passwort kurz genug handelt es sich bei der Verschlüsselung sogar um
ein „One-Time-Pad“ — bei bestimmungsgemäßem Einsatz nachweisbar sicher, toll!
Nur leider ist der Einsatz hier nicht wirklich *bestimmungsgemäß*, die
Sicherheit hängt davon ab, dass der Key unbekannt ist.
Da es nun nicht sonderlich sinnvoll ist, den Nutzer ein Passwort als Key
eingeben zu lassen um das gespeicherte Passwort zu entschlüsseln, muss der Key
entweder dynamisch generiert und unverschlüsselt ebenfalls abgespeichert werden,
oder halt statisch von der App festgelegt werden. Hier letzteres.

Was bringt uns das Ganze nun?

- Ohne root- oder physischen Zugriff ist es für einen Angreifer nicht möglich
  über diese App an euer AoD-Passwort zu gelangen.
- Ein ungerichteter Angriff eines Trojaners, der sich root-Rechte erschlichen
  hat um eure SharedPreferences abzukrabbeln wird wohl ebenfalls nichts mit dem
  gespeicherten Passwort anfangen können.
- Ein gezielter Angriff mit root- oder physischem Zugriff wäre bei
  entsprechender Motivation allerdings in der Lage das Passwort zu
  rekonstruieren in dem die zugehörige APK dekompiliert und der Code analysiert
  wird.

Willst du nun also wirklich unbedingt dein Passwort nie wieder in der App
eingeben müssen, die AoD-eigene „Login merken“-Funktion reicht dir nicht und aus
irgendeinem Grund willst auch bloß keinen Passwortmanager einsetzen (*„da muss
man ja sich auch immer noch ein Passwort für alle anderen Passwörter merken!“*),
aber du machst dir trotzdem Gedanken darum wie sicher der appinterne
Loginspeicher ist, dann musst du dich also fragen:

    „Würde jemanden gezielt mein Android Gerät für mein AoD-Passwort angreifen?“

Falls ja, dann solltest du vielleicht doch einfach einen anständigen
Passwortmanager wie KeypassDX_ verwenden. Oder es einfach bei AoDs „Login
merken“ belassen.
Und dich fragen woher Personen mit hinreichend krimineller Energie überhaupt,
genug über dich wissen um dein AoD-Passwort interessant zu finden.



Implementierung des ganzen Hokus-Pokus in ``GeckoAutofillHandler.java``.


.. [#length] Theoretisch könnte Malware die langfristig root-Zugriff erlangen
   konnte den Inhalt überwachen und bemerken, wie sich der Inhalt des zufällig
   generierten Füllraums mit der Zeit ändert, und damit die maximale
   Passwortlänge schließen.
   Eine solche Malware könnte realistisch gesehen aber auch einfach direkt einen
   Keylogger einsetzen und auch ohne, dass das Passwort abgespeichert wird eben
   jenes direkt erhalten.

.. _KeypassDX: https://f-droid.org/en/packages/com.kunzisoft.keepass.libre/
