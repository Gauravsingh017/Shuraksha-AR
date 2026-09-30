package com.example.data

data class VocationalSop(
    val moduleId: String,
    val title: String,
    val titleHi: String,
    val statutoryRef: String,
    val summary: String,
    val summaryHi: String,
    val keyThresholds: List<Pair<String, String>>,
    val keyThresholdsHi: List<Pair<String, String>>,
    val sopSteps: List<String>,
    val sopStepsHi: List<String>,
    val dosAndDonts: List<Pair<String, String>>,
    val dosAndDontsHi: List<Pair<String, String>>,
    val drillAudioEn: String,
    val drillAudioHi: String,
    val emergencyCondition: String = "",
    val emergencyConditionHi: String = "",
    val emergencySteps: List<String> = emptyList(),
    val emergencyStepsHi: List<String> = emptyList(),
    val dgmsLaws: String = "",
    val dgmsLawsHi: String = "",
    val dgmsLawsAudioEn: String = "",
    val dgmsLawsAudioHi: String = "",
    val manualQuizTips: List<String> = emptyList(),
    val manualQuizTipsHi: List<String> = emptyList()
)

data class QuizQuestion(
    val id: String,
    val moduleId: String,
    val question: String,
    val questionHi: String,
    val options: List<String>,
    val optionsHi: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val explanationHi: String
)

object TrainingContentRepository {

    val vocationalGuides: Map<String, VocationalSop> = mapOf(
        "1" to VocationalSop(
            moduleId = "1",
            title = "Underground Gas & Mine Ventilation",
            titleHi = "भूमिगत गैस और खदान वेंटिलेशन",
            statutoryRef = "DGMS Coal Mines Regulations (CMR) 2017 - Reg 169 & 170",
            summary = "Strict vocational guidelines for monitoring inflammable and noxious gases (Methane CH4, Carbon Monoxide CO) and maintaining effective ventilation currents in return and intake drifts.",
            summaryHi = "ज्वलनशील और जहरीली गैसों (मीथेन और कार्बन मोनोऑक्साइड) की निरंतर निगरानी और खदान में ताजी हवा के बहाव को बनाए रखने के लिए अनिवार्य व्यावसायिक नियम।",
            keyThresholds = listOf(
                "CH4 0.8% Alert" to "Mandatory investigation of roof cavities and auxiliary fan velocity.",
                "CH4 1.2% Cut-off" to "Immediate electric isolation of heading and withdrawal of all workforce.",
                "CO 25 PPM" to "Maximum allowable 8-hour shift ceiling before spontaneous combustion alert.",
                "Oxygen 19.0%" to "Statutory minimum allowable oxygen concentration by volume."
            ),
            keyThresholdsHi = listOf(
                "मीथेन 0.8% चेतावनी" to "छत की गुहाओं और पंखे की गति की अनिवार्य जांच।",
                "मीथेन 1.2% कट-ऑफ" to "तुरंत बिजली बंद करना और सभी श्रमिकों को बाहर निकालना।",
                "कार्बन मोनोऑक्साइड 25 PPM" to "8 घंटे की शिफ्ट में अधिकतम सुरक्षित सीमा।",
                "ऑक्सीजन 19.0%" to "हवा में ऑक्सीजन की न्यूनतम वैधानिक मात्रा।"
            ),
            sopSteps = listOf(
                "Step 1: Perform zero-calibration of the handheld multi-gas detector in pure intake surface atmosphere before descending shaft.",
                "Step 2: Approach blind heading testing every 10 meters; sample high roof cavities where methane accumulates due to lower specific gravity (0.55).",
                "Step 3: Inspect auxiliary brattice cloth and ventilation tubing for tearing, crushing, or decoupling.",
                "Step 4: If gas exceeds 1.2%, activate mechanical isolation valve immediately and sound the acoustic hazard horn.",
                "Step 5: Log telemetry reading in the statutory pit-bottom logbook and notify the Mine Overman immediately."
            ),
            sopStepsHi = listOf(
                "चरण 1: खदान में उतरने से पहले सतह की ताजी हवा में गैस डिटेक्टर का शून्य-कैलिब्रेशन करें।",
                "चरण 2: हर 10 मीटर पर गैस की जांच करें, खासकर छत के पास जहां हल्की मीथेन गैस जमा होती है।",
                "चरण 3: वेंटिलेशन डक्ट और ब्रैटीस क्लॉथ की जांच करें कि कहीं कोई कटाव या लीकेज तो नहीं है।",
                "चरण 4: यदि मीथेन 1.2% से अधिक हो, तो तुरंत आइसोलेशन वॉल्व बंद करें और अलार्म बजाएं।",
                "चरण 5: लॉगबुक में रीडिंग दर्ज करें और तुरंत माइन ओवरमैन को सूचित करें।"
            ),
            dosAndDonts = listOf(
                "DO keep ventilation stopping doors firmly shut after passing through to prevent air short-circuiting." to
                    "DON'T open stopping doors simultaneously or prop them open with timber chocks.",
                "DO use intrinsically safe / flameproof certified detectors only." to
                    "DON'T carry non-flameproof battery torches, electronics, or lighters underground.",
                "DO report unusual sulfurous or petrolic odors immediately." to
                    "DON'T enter abandoned or unventilated galleries under any circumstance."
            ),
            dosAndDontsHi = listOf(
                "वेंटिलेशन दरवाजों से गुजरने के बाद उन्हें हमेशा तुरंत बंद रखें।" to
                    "वेंटिलेशन दरवाजों को खुला न छोड़ें और लकड़ी के गुटके से न अटकाएं।",
                "केवल प्रमाणित फ्लेमप्रूफ और सुरक्षित उपकरणों का ही उपयोग करें।" to
                    "खदान के अंदर सामान्य टॉर्च, मोबाइल या लाइटर बिल्कुल न ले जाएं।",
                "किसी भी असामान्य गंध की सूचना तुरंत अधिकारियों को दें।" to
                    "बंद या हवा-रहित पुराने रास्तों में कभी प्रवेश न करें।"
            ),
            drillAudioEn = "Warning: High Methane gas leakage detected at 1.48%. Toxic plumes are billowing near the drift roof. Auxiliary ventilation compromised. Locate the glowing yellow valve and tap it to isolate the gas pipeline immediately.",
            drillAudioHi = "चेतावनी: खदान में 1.48 प्रतिशत मीथेन गैस का खतरनाक रिसाव हो रहा है। छत के पास जहरीला धुआं निकल रहा है। गैस पाइपलाइन को बंद करने के लिए चमकते हुए पीले वॉल्व पर तुरंत टैप करें।",
            emergencyCondition = "Methane (CH4) Gas Leak Exceeding 1.20% Cutoff Limit",
            emergencyConditionHi = "छत के पास 1.20% से अधिक मीथेन गैस का खतरनाक रिसाव",
            emergencySteps = listOf(
                "Step 1: Immediately isolate all electrical power supply to the affected district (CMR Reg 169).",
                "Step 2: Shut down auxiliary machines immediately to prevent ignition sparks.",
                "Step 3: Evacuate all personnel immediately along intake fresh airway. Never use compressed air hose (causes electrostatic sparks).",
                "Step 4: Rotate mechanical isolation valve to firmly isolate the ruptured gas line.",
                "Step 5: Erect danger barricade across gallery and notify Mine Overman and Ventilation Officer immediately."
            ),
            emergencyStepsHi = listOf(
                "चरण 1: पूरे कार्यक्षेत्र की बिजली तुरंत मुख्य स्विच से काटें (सीएमआर नियम 169)।",
                "चरण 2: स्पार्क या चिंगारी रोकने के लिए सभी मशीनें तुरंत बंद करें।",
                "चरण 3: ताजी हवा के रास्ते से सभी कामगारों को तुरंत बाहर निकालें। कम्प्रेस्ड एयर पाइप का उपयोग बिल्कुल न करें।",
                "चरण 4: गैस पाइपलाइन को बंद करने के लिए पीले आइसोलेशन वॉल्व को पूरी तरह बंद करें।",
                "चरण 5: गैलरी के प्रवेश द्वार पर खतरे की बाड़ लगाएं और ओवरमैन व वेंटिलेशन अधिकारी को तुरंत सूचित करें।"
            ),
            dgmsLaws = "DGMS Coal Mines Regulations (CMR) 2017 - Regulation 169 & 170: Inflammable gas must never exceed 0.8% in intake airways and 1.25% in any working face. When CH4 reaches 1.20%, electrical power cut-off and immediate workforce withdrawal are legally mandatory. Auxiliary ventilation must deliver a minimum of 30 m3/minute per person at all active coal faces.",
            dgmsLawsHi = "डीजीएमएस कोयला खान विनियम 2017 - विनियम 169 और 170: ताजी हवा के रास्ते में मीथेन 0.8% और कार्यस्थल पर 1.25% से अधिक नहीं होनी चाहिए। मीथेन 1.20% पहुंचते ही बिजली काटना और श्रमिकों को बाहर निकालना कानूनी रूप से अनिवार्य है। प्रत्येक व्यक्ति के लिए न्यूनतम 30 घन मीटर प्रति मिनट ताजी हवा का बहाव अनिवार्य है।",
            dgmsLawsAudioEn = "Statutory DGMS Regulation Briefing under Coal Mines Regulations 2017, Regulation 169. When inflammable methane gas reaches 1.2 percent, power cutoff is legally mandatory and all workers must evacuate to fresh intake air. Never prop open ventilation doors.",
            dgmsLawsAudioHi = "डीजीएमएस कोयला खान विनियम 2017 नियम 169 और 170 के वैधानिक नियम: खदान में मीथेन गैस 1.2 प्रतिशत पहुंचते ही बिजली काटना और मजदूरों को बाहर निकालना कानूनी रूप से अनिवार्य है। वेंटिलेशन दरवाजों को कभी खुला न छोड़ें।",
            manualQuizTips = listOf(
                "CH4 Statutory Cut-Off: 1.2% mandates immediate electric cutoff and workforce withdrawal.",
                "Intake Air Maximum: Methane must never exceed 0.8% in general intake airways.",
                "Carbon Monoxide Limit: CO above 25 PPM indicates spontaneous heating/combustion in coal pillars.",
                "Ventilation Stopping Doors: Must remain closed to prevent short-circuiting fresh intake air."
            ),
            manualQuizTipsHi = listOf(
                "मीथेन वैधानिक कट-ऑफ: 1.2% पर बिजली काटना और श्रमिकों की निकासी अनिवार्य है।",
                "ताजी हवा में सीमा: सामान्य इनटेक रास्ते में मीथेन 0.8% से कम होनी चाहिए।",
                "कार्बन मोनोऑक्साइड सीमा: 25 PPM से अधिक CO कोयले में स्वतः दहन का संकेत है।",
                "वेंटिलेशन दरवाजे: ताजी हवा के सीधे बाईपास को रोकने के लिए दरवाजे हमेशा बंद रखें।"
            )
        ),
        "2" to VocationalSop(
            moduleId = "2",
            title = "Underground Fire & Emergency Evacuation",
            titleHi = "भूमिगत आग और आपातकालीन निकासी",
            statutoryRef = "DGMS Coal Mines Regulations (CMR) 2017 - Reg 141 & 142",
            summary = "Standard vocational escape protocol during underground conveyor belt, coal seam, or electrical substation fires. Emphasizes self-rescuer activation and illuminated egress navigation.",
            summaryHi = "खदान में आग लगने की स्थिति में मानक बचाव प्रोटोकॉल। सेल्फ-रेस्क्यूअर का तुरंत उपयोग और सुरक्षित रिफ्यूज बे की ओर सुरक्षित निकासी।",
            keyThresholds = listOf(
                "Escape Time 60 Min" to "Nominal protection duration provided by chemical oxygen self-rescuer.",
                "Visibility < 1 Meter" to "Requirement to navigate using tactile floor lifeline / wall guide wire.",
                "Refuge Bay Capacity" to "Sealed shelter with 48-hour breathable air buffer and borehole communication."
            ),
            keyThresholdsHi = listOf(
                "बचाव समय 60 मिनट" to "सेल्फ-रेस्क्यूअर द्वारा प्रदान की जाने वाली शुद्ध ऑक्सीजन की अवधि।",
                "दृश्यता 1 मीटर से कम" to "धुएं में फर्श की गाइड लाइन और तीरों के सहारे आगे बढ़ें।",
                "रिफ्यूज बे क्षमता" to "48 घंटे ताजी हवा और संचार प्रणाली से युक्त सुरक्षित कक्ष।"
            ),
            sopSteps = listOf(
                "Step 1: Upon acoustic fire siren or smoke alarm, immediately stop machinery and assess air current direction.",
                "Step 2: Unclip self-rescuer from harness, break tamper seal, insert rubber mouthpiece, and clamp nostrils securely.",
                "Step 3: Stay low beneath the thermal smoke layer where air temperature and carbon monoxide concentration are lowest.",
                "Step 4: Follow illuminated green 3D floor arrows and high-reflectivity egress signage toward the nearest refuge bay.",
                "Step 5: Enter refuge bay outer airlock, purge contaminants with compressed air line, and bolt inner hermetic bulkhead."
            ),
            sopStepsHi = listOf(
                "चरण 1: आग का अलार्म बजते ही मशीनें बंद करें और हवा के बहाव की दिशा देखें।",
                "चरण 2: कमर से सेल्फ-रेस्क्यूअर निकालें, सील तोड़ें, माउथपीस मुंह में लगाएं और नाक का क्लिप कसें।",
                "चरण 3: झुककर चलें क्योंकि धुआं और जहरीली गैसें छत की ओर उठती हैं।",
                "चरण 4: फर्श पर चमकते हुए हरे तीरों का पीछा करते हुए निकटतम रिफ्यूज बे की ओर बढ़ें।",
                "चरण 5: रिफ्यूज बे के बाहरी एयरलॉक में प्रवेश करें, हवा साफ करें और अंदर का दरवाजा बंद करें।"
            ),
            dosAndDonts = listOf(
                "DO breathe steadily and rhythmically through the self-rescuer." to
                    "DON'T remove mouthpiece to speak or shout inside smoke-filled drifts.",
                "DO guide team members who may have disorientation or reduced visibility." to
                    "DON'T travel against the designated green evacuation route into unventilated dead-ends.",
                "DO close fire-separation doors behind your egress path to starve fire of intake oxygen." to
                    "DON'T attempt to salvage heavy tools or personal belongings during evacuation."
            ),
            dosAndDontsHi = listOf(
                "सेल्फ-रेस्क्यूअर से शांत रहकर सामान्य गति से सांस लेते रहें।" to
                    "धुएं के अंदर बात करने या चिल्लाने के लिए माउथपीस कभी न निकालें।",
                "साथी मजदूरों की मदद करें और मिलकर बाहर निकलें।" to
                    "निर्धारित हरे रास्ते को छोड़कर किसी बंद सुरंग की तरफ न जाएं।",
                "पीछे के अग्नि-रोधी दरवाजे बंद करते जाएं ताकि आग को हवा न मिले।" to
                    "निकासी के दौरान औजार या निजी सामान बचाने की कोशिश न करें।"
            ),
            drillAudioEn = "Code Red Emergency: Active coal conveyor fire detected. Don your self-rescuer mask immediately and secure the nose clamp. Follow the illuminated green floor arrows along the safe walkway to the sealed refuge bay.",
            drillAudioHi = "आपातकालीन कोड रेड: कन्वेयर बेल्ट में आग लग गई है। तुरंत अपना सेल्फ-रेस्क्यूअर पहनें और नाक का क्लिप लगाएं। सुरक्षित रिफ्यूज बे की ओर जाने वाले फर्श के हरे तीरों का पालन करें।",
            emergencyCondition = "Active Coal Conveyor Fire with Thick Smoke & CO Accumulation",
            emergencyConditionHi = "कन्वेयर बेल्ट में भीषण आग, घना काला धुआं और जहरीली गैसें",
            emergencySteps = listOf(
                "Step 1: Don chemical oxygen Self-Rescuer (SCSR) within 15 seconds; clamp nose clip firmly.",
                "Step 2: Never remove mouthpiece inside smoke to shout or communicate (single CO breath can be fatal).",
                "Step 3: Drop low beneath thermal smoke layer (within 0.5m of floor) where air is cooler and least toxic.",
                "Step 4: Keep one hand on tactile lifeline / floor guide wire, following illuminated green arrows toward refuge chamber.",
                "Step 5: Enter refuge bay outer airlock, purge contaminants with compressed air line, and bolt inner hermetic door."
            ),
            emergencyStepsHi = listOf(
                "चरण 1: 15 सेकंड के भीतर कमर से सेल्फ-रेस्क्यूअर निकालें, माउथपीस लगाएं और नाक का क्लिप कसें।",
                "चरण 2: बात करने या चिल्लाने के लिए धुएं में माउथपीस कभी न निकालें (एक सांस भी जानलेवा हो सकती है)।",
                "चरण 3: फर्श से 0.5 मीटर के दायरे में झुककर चलें जहां धुआं और गर्मी सबसे कम होती है।",
                "चरण 4: फर्श पर चमकते हरे तीरों और गाइड तार पर हाथ रखकर रिफ्यूज बे की ओर बढ़ें।",
                "चरण 5: रिफ्यूज बे के बाहरी एयरलॉक में प्रवेश करें, हवा साफ करें और अंदर का दरवाजा कसकर बंद करें।"
            ),
            dgmsLaws = "DGMS Coal Mines Regulations 2017 - Regulation 141 & 142: Every underground worker must carry an approved self-contained chemical oxygen self-rescuer providing minimum 60 minutes life support. Emergency refuge chambers must maintain 48 hours breathable atmosphere with independent surface borehole communications. Fire doors must be self-closing.",
            dgmsLawsHi = "डीजीएमएस कोयला खान विनियम 2017 - विनियम 141 और 142: भूमिगत खदान में काम करने वाले प्रत्येक व्यक्ति के पास न्यूनतम 60 मिनट ऑक्सीजन देने वाला सेल्फ-रेस्क्यूअर होना अनिवार्य है। आपातकालीन रिफ्यूज बे में 48 घंटे के लिए ताजी हवा और सतह से स्वतंत्र संचार प्रणाली होनी चाहिए। आग-रोधी दरवाजे स्वतः बंद होने वाले होने चाहिए।",
            dgmsLawsAudioEn = "Statutory DGMS Regulation Briefing under Coal Mines Regulations 2017, Regulation 141 and 142. In case of underground fire, deploy your self-rescuer within 15 seconds. Stay low under the thermal smoke layer and follow green chevrons to the sealed refuge bay. Never remove the mouthpiece in smoke.",
            dgmsLawsAudioHi = "डीजीएमएस कोयला खान विनियम 2017 नियम 141 और 142 के वैधानिक नियम: खदान में आग लगते ही 15 सेकंड में सेल्फ-रेस्क्यूअर पहनें। धुएं के नीचे झुककर फर्श के हरे तीरों के सहारे सुरक्षित रिफ्यूज बे में पहुंचें। धुएं में माउथपीस कभी न निकालें।",
            manualQuizTips = listOf(
                "Self-Rescuer Deployment: Must be donned within 15 seconds to prevent toxic CO inhalation.",
                "Smoke Egress Technique: Crawl below 0.5m floor level where air is cooler and least toxic.",
                "Refuge Bay Airlock: Outer door must be closed and purged before opening inner bulkhead door.",
                "Mouthpiece Rule: Never remove mouthpiece to shout; 0.5% CO causes sudden loss of consciousness."
            ),
            manualQuizTipsHi = listOf(
                "सेल्फ-रेस्क्यूअर पहनना: जहरीली गैस से बचने के लिए 15 सेकंड के भीतर पहनना अनिवार्य है।",
                "धुएं में निकासी तकनीक: फर्श से 0.5 मीटर नीचे झुककर चलें जहां तापमान और गैस न्यूनतम होती है।",
                "रिफ्यूज बे एयरलॉक: अंदर का दरवाजा खोलने से पहले बाहरी दरवाजा बंद करके हवा साफ करें।",
                "माउथपीस नियम: बात करने के लिए माउथपीस न निकालें; 0.5% CO तुरंत बेहोश कर देती है।"
            )
        ),
        "3" to VocationalSop(
            moduleId = "3",
            title = "Conveyor & Machinery Guarding (LOTO)",
            titleHi = "कन्वेयर और मशीनरी सुरक्षा (लोटो)",
            statutoryRef = "DGMS Technical Circular 04 of 2018 & CMR Reg 182",
            summary = "Vocational standard for isolating, locking out, tagging, and verifying zero-energy state on bulk coal belt conveyors, crushers, and pinch-point rotating drives.",
            summaryHi = "कोयला कन्वेयर बेल्ट और भारी मशीनों पर काम करते समय बिजली काटने, व्यक्तिगत ताला लगाने और शून्य यांत्रिक ऊर्जा की पुष्टि करने का नियम।",
            keyThresholds = listOf(
                "Zero Mechanical Energy" to "Verification that tension take-up pulleys and gravity counterweights are blocked.",
                "Pull-Cord Interval" to "Emergency pull-wire switch must be accessible every 100 meters along conveyor.",
                "Guard Clearance" to "Maximum 6mm mesh opening near nip points to prevent fingers/tools ingress."
            ),
            keyThresholdsHi = listOf(
                "शून्य ऊर्जा सत्यापन" to "मशीन की बिजली और काउंटर-वेट पूरी तरह से लॉक होने की पुष्टि।",
                "पुल-कॉर्ड दूरी" to "कन्वेयर के साथ हर 100 मीटर पर आपातकालीन स्टॉप तार होना अनिवार्य है।",
                "जाली सुरक्षा अंतर" to "घूमते पुर्जों के पास अधिकतम 6 मिमी की सुरक्षा जाली होनी चाहिए।"
            ),
            sopSteps = listOf(
                "Step 1: Notify the control room operator of the intended maintenance or clearing operation.",
                "Step 2: Actuate the local emergency trip wire pull-cord switch to mechanically latch the conveyor in emergency stop.",
                "Step 3: Rotate master electrical isolator to OFF and insert personal red padlock on the multi-lock hasp.",
                "Step 4: Affix standardized high-visibility danger tag with worker ID, name, date, and reason for isolation.",
                "Step 5: Perform zero-energy test: depress local start pushbutton to physically confirm drive motor will not spin.",
                "Step 6: Mechanically chock gravity take-up counterweight before working on tail pulley or belt splices."
            ),
            sopStepsHi = listOf(
                "चरण 1: मेंटेनेंस कार्य शुरू करने से पहले कंट्रोल रूम ऑपरेटर को सूचित करें।",
                "चरण 2: कन्वेयर को रोकने के लिए आपातकालीन ट्रिप-वायर पुल-कॉर्ड खींचें।",
                "चरण 3: मास्टर आइसोलेटर को ऑफ करें और मल्टी-लॉक हैस्प पर अपना लाल ताला लगाएं।",
                "चरण 4: अपना नाम, आईडी और कार्य विवरण लिखा हुआ 'खतरा' टैग लगाएं।",
                "चरण 5: स्टार्ट बटन दबाकर पुष्टि करें कि मोटर बिल्कुल नहीं घूम रही है।",
                "चरण 6: टेल पुली पर काम करने से पहले काउंटर-वेट को लकड़ी के गुटके से लॉक करें।"
            ),
            dosAndDonts = listOf(
                "DO lock out each individual tradesperson's personal lock on the hasp." to
                    "DON'T share padlocks or allow one worker to lock out on behalf of others.",
                "DO replace all protective mesh guards and tighten bolts before removing LOTO." to
                    "DON'T use wooden sticks or hand shovels to dislodge coal lumps while belt is running.",
                "DO test pull-cord functionality weekly with safety logbook entry." to
                    "DON'T tie down or bypass trip-cord switches with copper wire."
            ),
            dosAndDontsHi = listOf(
                "हर कारीगर अपना व्यक्तिगत ताला स्वयं लगाएगा।" to
                    "ताले की चाबी किसी और को न दें और किसी अन्य के भरोसे काम न करें।",
                "काम पूरा होने पर सुरक्षा जाली दोबारा कसकर लगाएं।" to
                    "चलती हुई कन्वेयर बेल्ट पर फावड़े या डंडे से कोयला हटाने की कोशिश न करें।",
                "आपातकालीन पुल-कॉर्ड की हर सप्ताह नियमित जांच करें।" to
                    "पुल-कॉर्ड को तार से बांधकर या बाईपास करके कभी काम न चलाएं।"
            ),
            drillAudioEn = "Danger: Exposed rotating conveyor pinch point drum. Moving nip hazard detected. Pull the emergency trip-cord, tap the red LOTO station to apply personal padlock, and verify zero mechanical energy before maintenance.",
            drillAudioHi = "खतरा: कन्वेयर बेल्ट का घूमने वाला हिस्सा खुला है। आपातकालीन पुल-कॉर्ड खींचें, लाल लोटो बॉक्स पर टैप करके ताला लगाएं और शून्य ऊर्जा की पुष्टि करें।",
            emergencyCondition = "Conveyor Belt Boulder Jam & Exposed Rotating Drum Pinch Hazard",
            emergencyConditionHi = "कन्वेयर बेल्ट में पत्थर फंसना और घूमने वाला असुरक्षित पुली ड्रम",
            emergencySteps = listOf(
                "Step 1: Pull the emergency pull-cord wire immediately to mechanically stop the conveyor.",
                "Step 2: Notify the surface control room operator before initiating any clearing or maintenance.",
                "Step 3: Rotate master electrical isolator to OFF and apply personal red padlock on multi-lock hasp (One person, one lock, one key).",
                "Step 4: Affix standardized high-visibility danger tag with worker ID, name, date, and task details.",
                "Step 5: Depress local start pushbutton to physically verify zero mechanical motion (zero-energy test).",
                "Step 6: Mechanically chock gravity take-up counterweight before touching tail pulley or removing jammed coal boulder."
            ),
            emergencyStepsHi = listOf(
                "चरण 1: कन्वेयर को तुरंत रोकने के लिए आपातकालीन ट्रिप-वायर पुल-कॉर्ड खींचें।",
                "चरण 2: कोई भी काम शुरू करने से पहले कंट्रोल रूम ऑपरेटर को सूचित करें।",
                "चरण 3: मास्टर आइसोलेटर को ऑफ करें और मल्टी-लॉक हैस्प पर अपना लाल ताला लगाएं (एक व्यक्ति, एक ताला, एक चाबी)।",
                "चरण 4: अपना नाम, आईडी, तारीख और कार्य लिखा हुआ 'खतरा' टैग लगाएं।",
                "चरण 5: स्टार्ट बटन दबाकर भौतिक रूप से पुष्टि करें कि मशीन बिल्कुल नहीं घूम रही (शून्य ऊर्जा परीक्षण)।",
                "चरण 6: टेल पुली या पत्थर छूने से पहले काउंटर-वेट को लकड़ी के गुटके से पूरी तरह ब्लॉक करें।"
            ),
            dgmsLaws = "DGMS Technical Circular 04 of 2018 & CMR 2017 Regulation 182: Mandates dual pull-wire emergency stop switches along the full span of all conveyor walkways, spaced not more than 100 meters apart. Strict Lock-Out / Tag-Out (LOTO) protocol is mandatory before cleaning or repairing. Working on or cleaning energized conveyors is a punishable offense.",
            dgmsLawsHi = "डीजीएमएस तकनीकी परिपत्र 04/2018 और सीएमआर 2017 विनियम 182: कन्वेयर बेल्ट के साथ हर 100 मीटर पर आपातकालीन ट्रिप पुल-कॉर्ड होना अनिवार्य है। मेंटेनेंस से पहले पूर्ण लॉक-आउट/टैग-आउट (लोटो) का पालन अनिवार्य है। चलती हुई मशीन पर काम करना या पत्थर हटाना कानूनी रूप से दंडनीय अपराध है।",
            dgmsLawsAudioEn = "Statutory DGMS Regulation Briefing under Technical Circular 04 of 2018 and CMR Regulation 182. Every technician must apply their own personal padlock on the multi-lock hasp. Depress start button to verify zero mechanical energy. Never clear jammed coal from running belts.",
            dgmsLawsAudioHi = "डीजीएमएस तकनीकी परिपत्र 04/2018 और सीएमआर नियम 182 के वैधानिक नियम: प्रत्येक कारीगर मल्टी-लॉक क्लैंप पर अपना व्यक्तिगत ताला स्वयं लगाएगा। स्टार्ट बटन दबाकर शून्य ऊर्जा की पुष्टि करें। चलती बेल्ट से पत्थर हटाने की कोशिश कभी न करें।",
            manualQuizTips = listOf(
                "Golden LOTO Rule: One Person, One Lock, One Key. No worker depends on someone else's padlock.",
                "Zero-Energy Test: Depress local start button to confirm motor does not spin before touching machinery.",
                "Pull-Cord Interval: Emergency trip wire must be accessible every 100 meters along conveyor.",
                "Mesh Guard Safety: Protective fixed guards must have maximum 6mm mesh openings near rotating nip points."
            ),
            manualQuizTipsHi = listOf(
                "लोटो का स्वर्णिम नियम: एक व्यक्ति, एक ताला, एक चाबी। किसी अन्य के ताले पर भरोसा न करें।",
                "शून्य ऊर्जा परीक्षण: मशीन छूने से पहले स्टार्ट बटन दबाकर पुष्टि करें कि मोटर नहीं घूम रही।",
                "पुल-कॉर्ड दूरी: कन्वेयर के साथ हर 100 मीटर पर आपातकालीन स्टॉप तार होना अनिवार्य है।",
                "सुरक्षा जाली: घूमते पुर्जों के पास अधिकतम 6 मिमी की जाली होनी चाहिए ताकि उंगलियां अंदर न जा सकें।"
            )
        ),
        "4" to VocationalSop(
            moduleId = "4",
            title = "Electrical Isolation & Arc Flash Safety",
            titleHi = "विद्युत अलगाव और आर्क फ्लैश सुरक्षा",
            statutoryRef = "Central Electricity Authority (Measures relating to Safety and Electric Supply) Reg 116 & DGMS CMR Reg 155",
            summary = "Safe isolation procedures for underground high-voltage (3.3kV / 6.6kV) flameproof substations, transformer switchgears, and trailing cable gate-end boxes.",
            summaryHi = "खदान के 3.3 केवी फ्लेमप्रूफ सबस्टेशन और ट्रांसफार्मर पर सुरक्षित काम करने, बिजली काटने और अर्थिंग करने के वैधानिक नियम।",
            keyThresholds = listOf(
                "Arc Flash Boundary 1.5m" to "Minimum safe boundary requiring NFPA 70E / IS rated arc flash PPE.",
                "Earth Leakage 750mA" to "Instantaneous trip sensitivity required on all underground electrical feeders.",
                "Discharge Time 5 Min" to "Required wait time for internal power capacitors to discharge safely to ground."
            ),
            keyThresholdsHi = listOf(
                "आर्क फ्लैश सीमा 1.5 मीटर" to "इस दायरे में विशेष आर्क-रेटेड सूट और फेस शील्ड अनिवार्य है।",
                "अर्थ लीकेज 750mA" to "खदान में बिजली के फाल्ट पर तुरंत ट्रिप होने की संवेदनशील सीमा।",
                "डिस्चार्ज समय 5 मिनट" to "कैपेसिटर की अवशिष्ट बिजली को ग्राउंड होने के लिए आवश्यक प्रतीक्षा।"
            ),
            sopSteps = listOf(
                "Step 1: Obtain a signed Electrical Permit-To-Work (PTW) from the Colliery Electrical Engineer.",
                "Step 2: Don Category 4 arc-rated face shield, dielectric gloves (10kV tested), and flame-resistant overalls.",
                "Step 3: Open upstream vacuum circuit breaker (VCB) and rack breaker into isolated test position.",
                "Step 4: Engage the mechanical earth knife switch to bond all high-voltage busbars directly to mine ground.",
                "Step 5: Use a certified non-contact high-voltage audio/visual potential tester to confirm 0.0V potential on all phases.",
                "Step 6: Fit padlock on the earth switch handle and apply yellow 'MEN AT WORK ON LINE' caution placard."
            ),
            sopStepsHi = listOf(
                "चरण 1: कोलियरी इलेक्ट्रिकल इंजीनियर से हस्ताक्षरित परमिट-टू-वर्क (PTW) प्राप्त करें।",
                "चरण 2: श्रेणी 4 की आर्क फेस शील्ड, 10kV इंसुलेटेड दस्ताने और सुरक्षा सूट पहनें।",
                "चरण 3: वैक्यूम सर्किट ब्रेकर (VCB) को खोलें और आइसोलेटेड स्थिति में रैक आउट करें।",
                "चरण 4: अर्थ नाइफ स्विच को जोड़कर सभी हाई-वोल्टेज तारों को खदान अर्थिंग से जोड़ें।",
                "चरण 5: हाई-वोल्टेज डिटेक्टर से जांचें कि तीनों फेज़ में वोल्टेज बिल्कुल शून्य है।",
                "चरण 6: अर्थ हैंडल पर ताला लगाएं और 'लाइन पर काम चालू है' का चेतावनी बोर्ड लगाएं।"
            ),
            dosAndDonts = listOf(
                "DO verify zero voltage on all 3 phases (R-Y-B) individually." to
                    "DON'T assume a circuit is dead just because indicator pilot lamp is unlit.",
                "DO keep flameproof enclosure flanged joints clean and free from paint or coal dust." to
                    "DON'T omit high-tensile flanged bolts or use non-standard hardware on flameproof enclosures.",
                "DO report damaged trailing cables with outer rubber punctures immediately." to
                    "DON'T reset tripped earth leakage breaker without inspecting trailing cable for cuts."
            ),
            dosAndDontsHi = listOf(
                "तीनों फेज़ (R-Y-B) में वोल्टेज की व्यक्तिगत रूप से पुष्टि करें।" to
                    "केवल इंडिकेटर बल्ब बुझा देखकर यह न मानें कि लाइन में करंट नहीं है।",
                "फ्लेमप्रूफ बॉक्स के जोड़ों को हमेशा साफ रखें और कभी पेंट न करें।" to
                    "फ्लेमप्रूफ बॉक्स में साधारण नट-बोल्ट का उपयोग न करें और कोई बोल्ट अधूरा न छोड़ें।",
                "कटी हुई बिजली केबल की तुरंत रिपोर्ट करें।" to
                    "केबल की पूरी जांच किए बिना ट्रिप हुए अर्थ लीकेज ब्रेकर को दोबारा चालू न करें।"
            ),
            drillAudioEn = "Warning: 3.3 kilovolt flameproof electrical substation arcing with dangerous plasma sparks. Don Category 4 arc-rated gear, open the vacuum circuit breaker, and tap the rotary isolator to engage earth knife grounding.",
            drillAudioHi = "सावधान: 3.3 केवी सबस्टेशन में खतरनाक इलेक्ट्रिक स्पार्क और प्लाज्मा निकल रहा है। वैक्यूम सर्किट ब्रेकर को बंद करें और अर्थ नाइफ स्विच से ग्राउंडिंग के लिए आइसोलेटर पर टैप करें।",
            emergencyCondition = "High Voltage 3.3kV Flameproof Substation Arcing & Earth Fault",
            emergencyConditionHi = "3.3 केवी सबस्टेशन में खतरनाक इलेक्ट्रिक आर्क फ्लैश और अर्थ फॉल्ट",
            emergencySteps = listOf(
                "Step 1: Obtain a signed Electrical Permit-To-Work (PTW) from Colliery Electrical Engineer.",
                "Step 2: Don Category 4 arc-rated face shield, 10kV dielectric gloves, and flame-resistant overalls.",
                "Step 3: Open upstream vacuum circuit breaker (VCB) and rack breaker into isolated test position.",
                "Step 4: Engage mechanical earth knife switch to bond all high-voltage busbars directly to mine ground.",
                "Step 5: Test all 3 phases (R-Y-B) with certified high-voltage audio/visual potential tester to confirm 0.0V.",
                "Step 6: Padlock earth switch handle and hang 'MEN AT WORK ON LINE' caution placard."
            ),
            emergencyStepsHi = listOf(
                "चरण 1: कोलियरी इलेक्ट्रिकल इंजीनियर से हस्ताक्षरित परमिट-टू-वर्क (PTW) प्राप्त करें।",
                "चरण 2: श्रेणी 4 की आर्क फेस शील्ड, 10kV इंसुलेटेड दस्ताने और सुरक्षा सूट पहनें।",
                "चरण 3: वैक्यूम सर्किट ब्रेकर (VCB) को खोलें और आइसोलेटेड स्थिति में रैक आउट करें।",
                "चरण 4: अर्थ नाइफ स्विच को जोड़कर सभी हाई-वोल्टेज तारों को खदान अर्थिंग से जोड़ें।",
                "चरण 5: हाई-वोल्टेज डिटेक्टर से जांचें कि तीनों फेज़ (R-Y-B) में वोल्टेज बिल्कुल शून्य है।",
                "चरण 6: अर्थ हैंडल पर ताला लगाएं और 'लाइन पर काम चालू है' का चेतावनी बोर्ड लगाएं।"
            ),
            dgmsLaws = "Central Electricity Authority (Safety & Electric Supply) Regulations Reg 116 & DGMS CMR Reg 155: Arc flash safe approach boundary is minimum 1.5 meters. Underground electrical feeders must be protected by instantaneous Earth Leakage Relays with 750mA trip sensitivity. Flameproof enclosure flanged joints must be kept clean, unpainted, with max gap 0.4mm.",
            dgmsLawsHi = "केंद्रीय विद्युत प्राधिकरण विनियम 116 और डीजीएमएस सीएमआर विनियम 155: आर्क फ्लैश का सुरक्षित दायरा कम से कम 1.5 मीटर है। खदान के सभी फीडरों में 750mA संवेदनशीलता वाला अर्थ लीकेज रिले अनिवार्य है। रिले को बाईपास करना दंडनीय अपराध है। फ्लेमप्रूफ बक्से के जोड़ों को साफ और बिना पेंट रखें।",
            dgmsLawsAudioEn = "Statutory DGMS and Central Electricity Authority Regulation Briefing under CEA Regulation 116 and CMR Regulation 155. Earth leakage relays must trip instantaneously at 750 milliamps. Never bridge or bypass safety trip relays. Always verify zero voltage on all three phases.",
            dgmsLawsAudioHi = "डीजीएमएस और केंद्रीय विद्युत प्राधिकरण नियम 116 व सीएमआर नियम 155 के वैधानिक नियम: अर्थ लीकेज रिले 750 मिली-एम्पीयर पर तुरंत ट्रिप होना चाहिए। रिले को तार से बाईपास करना गैर-कानूनी है। तीनों फेज़ में शून्य वोल्टेज जांचने के बाद ही काम करें।",
            manualQuizTips = listOf(
                "Arc Flash Boundary: Minimum 1.5 meters requiring NFPA/IS rated Category 4 PPE.",
                "Earth Leakage Sensitivity: Mandatory 750mA instantaneous trip sensitivity on all feeders.",
                "Never Bypass Relays: Bridging earth leakage contacts with wire is a criminal violation.",
                "Flameproof Joint Principle: Machined gap (0.4mm) quenches internal flames so ambient methane cannot ignite."
            ),
            manualQuizTipsHi = listOf(
                "आर्क फ्लैश सीमा: न्यूनतम 1.5 मीटर जहां विशेष श्रेणी 4 सुरक्षा सूट अनिवार्य है।",
                "अर्थ लीकेज संवेदनशीलता: सभी फीडरों पर 750mA पर तुरंत ट्रिप होना अनिवार्य है।",
                "रिले बाईपास न करें: अर्थ लीकेज रिले को तार से बाईपास करना गंभीर अपराध है।",
                "फ्लेमप्रूफ सिद्धांत: जोड़ों का बारीक अंतर (0.4 मिमी) चिंगारी को ठंडा करके बुझा देता है।"
            )
        ),
        "5" to VocationalSop(
            moduleId = "5",
            title = "Mandatory PPE & Mine Site Access Verification",
            titleHi = "अनिवार्य पीपीई और खदान प्रवेश जांच",
            statutoryRef = "DGMS Mines Vocational Training Rules 1966 & CMR 2017 Reg 198",
            summary = "Vocational muster checklist and mandatory equipment validation required before descending pit cage or working at active open-cast / underground faces.",
            summaryHi = "खदान में उतरने से पहले प्रत्येक कामगार के लिए अनिवार्य सुरक्षा उपकरण (हेलमेट, कैप लैंप, सेल्फ-रेस्क्यूअर, जूते) की जांच के वैधानिक नियम।",
            keyThresholds = listOf(
                "Cap Lamp Beam 12+ Hours" to "Statutory minimum duration of continuous battery illumination at 1500+ lux.",
                "Helmet Impact 50 Joules" to "DGMS approved fiber reinforced polycarbonate safety helmet rating.",
                "Boot Resistance" to "Anti-static conductive sole between 100 kilo-ohms and 1000 mega-ohms.",
                "Self-Rescuer Weight" to "Must be weighed monthly; weight increase >15g indicates hazardous moisture ingress."
            ),
            keyThresholdsHi = listOf(
                "कैप लैंप रोशनी 12+ घंटे" to "लगातार 12 घंटे तक तेज रोशनी देने वाली बैटरी की वैधानिक आवश्यकता।",
                "हेलमेट सुरक्षा 50 जूल" to "डीजीएमएस प्रमाणित पॉलीकार्बोनेट सुरक्षा हेलमेट रेटिंग।",
                "एंटी-स्टैटिक जूते" to "स्थैतिक बिजली के स्पार्क को रोकने वाले विशेष सुरक्षा जूते।",
                "सेल्फ-रेस्क्यूअर वजन" to "मासिक वजन जांच; 15 ग्राम से अधिक वजन नमी घुसने का संकेत है।"
            ),
            sopSteps = listOf(
                "Step 1: Check-in at the lamp room: inspect cap lamp lens, cable strain relief, and confirm green battery charging indicator.",
                "Step 2: Inspect Self-Rescuer apparatus: ensure tamper indicator wire is intact, casing is free from deep dents, and moisture indicator bead is blue/green (not pink).",
                "Step 3: Inspect industrial safety helmet: check 4-point internal harness webbing clearance and chin strap elasticity.",
                "Step 4: Inspect anti-static steel-toe boots: check for oil cracks and metallic sole plate integrity.",
                "Step 5: Hand over all contraband (mobile phones, matchboxes, tobacco, smartwatches) at the pit-head security muster desk.",
                "Step 6: Receive brass identification token and register biometric entry before boarding cage."
            ),
            sopStepsHi = listOf(
                "चरण 1: लैंप रूम में कैप लैंप का शीशा, केबल और हरी चार्जिंग लाइट की जांच करें।",
                "चरण 2: सेल्फ-रेस्क्यूअर की सील, तार और नमी सूचक (नीला/हरा होना चाहिए, गुलाबी नहीं) की जांच करें।",
                "चरण 3: सुरक्षा हेलमेट के अंदरूनी पट्टे और ठोड़ी के स्ट्रैप की मजबूती जांचें।",
                "चरण 4: स्टील-टो सुरक्षा जूतों की जांच करें कि कोई दरार तो नहीं है।",
                "चरण 5: प्रतिबंधित वस्तुएं (मोबाइल, बीड़ी, माचिस, स्मार्टवॉच) सुरक्षा डेस्क पर जमा करें।",
                "चरण 6: पीतल का टोकन प्राप्त करें और बायोमेट्रिक हाजिरी लगाकर ही केज में चढ़ें।"
            ),
            dosAndDonts = listOf(
                "DO wear chin strap secured properly under chin at all times underground." to
                    "DON'T wear cap backward or place padding/caps underneath safety helmet.",
                "DO carry the self-rescuer on the waist belt whenever underground." to
                    "DON'T leave self-rescuer resting on machinery or underground electrical boxes.",
                "DO inspect safety goggles and ear defenders before operating face drills." to
                    "DON'T enter the pit with damaged boots or without statutory high-visibility retro-reflective bands."
            ),
            dosAndDontsHi = listOf(
                "खदान में हमेशा हेलमेट की ठोड़ी का पट्टा कसकर बांधे रखें।" to
                    "हेलमेट के नीचे गमछा या टोपी न लगाएं और हेलमेट को उल्टा न पहनें।",
                "सेल्फ-रेस्क्यूअर को हमेशा अपनी कमर की पेटी पर बांधकर रखें।" to
                    "सेल्फ-रेस्क्यूअर को किसी मशीन या बिजली के बक्से पर रखकर न भूलें।",
                "ड्रिलिंग के समय सुरक्षा चश्मा और कान के प्लग जरूर पहनें।" to
                    "बिना रिफ्लेक्टिव पट्टी वाली वर्दी या फटे जूतों के साथ खदान में न जाएं।"
            ),
            drillAudioEn = "DGMS Pre-shift muster inspection. Verify your self-rescuer hermetic seal, 12-hour cap lamp beam, and anti-static boots. Surrender contraband mobile phones and tap the workbench to complete verification.",
            drillAudioHi = "डीजीएमएस प्री-शिफ्ट मस्टर जांच। अपने सेल्फ-रेस्क्यूअर, 12 घंटे की कैप लैंप और एंटी-स्टैटिक जूतों की जांच करें। मोबाइल फोन जमा करें और वर्कबेंच पर टैप करके जांच पूरी करें।",
            emergencyCondition = "Pre-Shift Muster Inspection: Prohibited Contraband & Defective Equipment",
            emergencyConditionHi = "शिफ्ट पूर्व मस्टर जांच: प्रतिबंधित सामग्री और खराब सुरक्षा उपकरण",
            emergencySteps = listOf(
                "Step 1: Inspect Cap Lamp: confirm green battery charging status and 12+ hours continuous beam at 1500 lux.",
                "Step 2: Inspect Self-Rescuer: ensure tamper wire is intact, body free of dents, moisture indicator is blue/green (not pink).",
                "Step 3: Check safety helmet 4-point webbing clearance and secure chin strap under chin at all times.",
                "Step 4: Inspect anti-static steel-toe boots (100kΩ - 1000MΩ resistance) for cracked soles.",
                "Step 5: Surrender all contraband (mobile phone, smartwatches, matchboxes, tobacco) at pit-head muster desk.",
                "Step 6: Collect brass token and register biometric entry before boarding cage."
            ),
            emergencyStepsHi = listOf(
                "चरण 1: लैंप रूम में कैप लैंप का शीशा, केबल और हरी चार्जिंग लाइट की जांच करें (12+ घंटे रोशनी)।",
                "चरण 2: सेल्फ-रेस्क्यूअर की सील, तार और नमी सूचक (नीला/हरा होना चाहिए, गुलाबी नहीं) की जांच करें।",
                "चरण 3: सुरक्षा हेलमेट के अंदरूनी पट्टे और ठोड़ी के स्ट्रैप की मजबूती जांचें।",
                "चरण 4: एंटी-स्टैटिक स्टील-टो जूतों की जांच करें कि कोई दरार तो नहीं है।",
                "चरण 5: प्रतिबंधित वस्तुएं (मोबाइल, बीड़ी, माचिस, स्मार्टवॉच) सुरक्षा डेस्क पर जमा करें।",
                "चरण 6: पीतल का टोकन प्राप्त करें और बायोमेट्रिक हाजिरी लगाकर ही केज में चढ़ें।"
            ),
            dgmsLaws = "DGMS Mines Vocational Training Rules 1966 & CMR 2017 Regulation 198: Zero tolerance for non-certified PPE underground. Under Section 67 of the Mines Act 1952, carrying contraband mobile phones, matches, or smoking materials into an underground coal mine is a cognizable criminal offense punishable with imprisonment.",
            dgmsLawsHi = "डीजीएमएस खान व्यावसायिक प्रशिक्षण नियम 1966 और सीएमआर 2017 विनियम 198: भूमिगत खदान में केवल प्रमाणित पीपीई की अनुमति है। खान अधिनियम 1952 की धारा 67 के तहत, खदान में मोबाइल फोन, माचिस, बीड़ी या लाइटर ले जाना गैर-जमानती आपराधिक अपराध है जिसमें कारावास का प्रावधान है।",
            dgmsLawsAudioEn = "Statutory DGMS Regulation Briefing under Mines Vocational Training Rules 1966 and CMR Regulation 198. Cap lamps must provide 12 continuous hours of illumination. Carrying non-intrinsically safe smartphones, matches, or lighters past pit-head security is strictly prohibited by law.",
            dgmsLawsAudioHi = "डीजीएमएस खान व्यावसायिक प्रशिक्षण नियम 1966 और सीएमआर नियम 198 के वैधानिक नियम: कैप लैंप कम से कम 12 घंटे लगातार तेज रोशनी देने वाली होनी चाहिए। खदान में मोबाइल, माचिस या लाइटर ले जाना कानूनन सख्त मना और दंडनीय अपराध है।",
            manualQuizTips = listOf(
                "Cap Lamp Statutory Duration: Minimum 12 continuous hours at certified lux output.",
                "Self-Rescuer Moisture Indicator: Must be blue/green; pink/white indicates spoiled canister.",
                "Antistatic Boots Purpose: Bleeds static charge safely to earth, preventing methane spark ignition.",
                "Contraband Law: Mobile phones and ordinary batteries are illegal contraband under Section 67 of Mines Act."
            ),
            manualQuizTipsHi = listOf(
                "कैप लैंप वैधानिक अवधि: कम से कम 12 घंटे लगातार प्रमाणित रोशनी।",
                "सेल्फ-रेस्क्यूअर नमी सूचक: नीला या हरा होना चाहिए; गुलाबी या सफेद होने पर तुरंत बदलें।",
                "एंटी-स्टैटिक जूते: शरीर के स्थैतिक आवेश को जमीन में विसर्जित करके गैस विस्फोट रोकते हैं।",
                "प्रतिबंधित वस्तु कानून: खान अधिनियम की धारा 67 के तहत मोबाइल और माचिस ले जाना अवैध अपराध है।"
            )
        )
    )

    val moduleQuestions: Map<String, List<QuizQuestion>> = mapOf(
        "1" to listOf(
            QuizQuestion(
                id = "1_1",
                moduleId = "1",
                question = "A handheld multi-gas detector reads 1.35% methane (CH4) near the drift roof heading. Under DGMS CMR 2017 regulations, what is the mandatory immediate action?",
                questionHi = "गैस डिटेक्टर में ड्रिफ्ट छत के पास 1.35% मीथेन (CH4) दर्ज की गई है। डीजीएमएस सीएमआर 2017 नियमों के तहत तुरंत क्या कार्रवाई अनिवार्य है?",
                options = listOf(
                    "Continue excavation while speeding up auxiliary ventilation fan",
                    "Isolate all electric power to the district immediately and withdraw all personnel to intake fresh air",
                    "Wait 20 minutes to verify whether the reading drops naturally below 1.0%",
                    "Direct the compressed air hose at the roof to dilute the localized gas pocket"
                ),
                optionsHi = listOf(
                    "काम जारी रखें और वेंटिलेशन पंखे की गति बढ़ा दें",
                    "तुरंत पूरे क्षेत्र की बिजली काट दें और सभी श्रमिकों को ताजी हवा में बाहर निकालें",
                    "20 मिनट प्रतीक्षा करें कि गैस अपने आप 1.0% से नीचे आ जाए",
                    "गैस हटाने के लिए संपीड़ित हवा (कम्प्रेस्ड एयर) का पाइप छत की ओर करें"
                ),
                correctIndex = 1,
                explanation = "Under CMR 2017 Reg 169, when inflammable gas exceeds 1.2% in any working place, all electrical power must be immediately isolated and all persons withdrawn to fresh intake air. Directing compressed air is hazardous as it can generate electrostatic sparks.",
                explanationHi = "सीएमआर नियम 169 के अनुसार, यदि ज्वलनशील गैस 1.2% से अधिक हो जाती है, तो तुरंत बिजली काटकर सभी कर्मचारियों को ताजी हवा में सुरक्षित बाहर निकालना अनिवार्य है। कम्प्रेश्ड एयर से स्थैतिक स्पार्क हो सकता है।"
            ),
            QuizQuestion(
                id = "1_2",
                moduleId = "1",
                question = "Why is it strictly prohibited to prop open ventilation stopping doors in underground mining drifts?",
                questionHi = "खदान में वेंटिलेशन रोकने वाले दरवाजों को खुला छोड़ना या लकड़ी से अटकाना सख्त मना क्यों है?",
                options = listOf(
                    "It causes coal dust to settle faster on the conveyor belt",
                    "It short-circuits the fresh intake airflow directly into the return airway, depriving active faces of oxygen and allowing toxic gases to accumulate",
                    "It wears out the door hinges and hydraulic closure mechanisms prematurely",
                    "It reduces the acoustic clarity of the pit-bottom telephone communications"
                ),
                optionsHi = listOf(
                    "इससे कन्वेयर बेल्ट पर कोयले की धूल तेजी से जमती है",
                    "यह ताजी हवा के बहाव को सीधे वापसी रास्ते में मोड़ देता है, जिससे कार्यस्थल पर ऑक्सीजन खत्म हो जाती है और जहरीली मीथेन गैस जमा हो जाती है",
                    "इससे दरवाजे के कब्जे और हाइड्रोलिक स्प्रिंग खराब हो जाते हैं",
                    "इससे भूमिगत टेलीफोन की आवाज साफ सुनाई नहीं देती"
                ),
                correctIndex = 1,
                explanation = "Leaving stopping doors open short-circuits the mine ventilation current. Fresh intake air bypasses the working face and dumps straight into the return drift, rapidly creating deadly, explosive methane pockets at active coal faces.",
                explanationHi = "दरवाजा खुला छोड़ने से ताजी हवा काम करने वाले चेहरे तक पहुंचे बिना सीधे वापसी रास्ते में निकल जाती है, जिससे कार्यस्थल पर जानलेवा विस्फोटक मीथेन गैस तुरंत जमा हो जाती है।"
            ),
            QuizQuestion(
                id = "1_3",
                moduleId = "1",
                question = "During inspection of a sealed old working area, your multi-gas detector alarm beeps for Carbon Monoxide (CO) at 28 PPM. What does this indicate?",
                questionHi = "पुराने सीलबंद क्षेत्र के पास गैस डिटेक्टर में कार्बन मोनोऑक्साइड 28 PPM पर अलार्म बजता है। यह क्या दर्शाता है?",
                options = listOf(
                    "Standard background emission from diesel haulage locomotives",
                    "Early warning indicator of spontaneous combustion (heating) in coal pillars or old goaf",
                    "Battery failure on the multi-gas detector unit",
                    "Excess moisture in the auxiliary ventilation ducting"
                ),
                optionsHi = listOf(
                    "डीजल लोकोमोटिव से निकलने वाला सामान्य धुआं",
                    "कोयला स्तंभों या पुराने गोफ में स्वतः दहन (भीतरी आग और हीटिंग) का प्रारंभिक चेतावनी संकेत",
                    "गैस डिटेक्टर की बैटरी खत्म होने का संकेत",
                    "वेंटिलेशन डक्ट में अत्यधिक नमी होना"
                ),
                correctIndex = 1,
                explanation = "Carbon monoxide (CO) is the primary chemical indicator of spontaneous heating/combustion of coal. A reading above 20 PPM in a return gallery requires immediate warning to the Overman and atmospheric bag sampling under Graham's Ratio protocol.",
                explanationHi = "कार्बन मोनोऑक्साइड कोयले में भीतरी आग (स्पॉन्टेनियस कम्बशन) का प्राथमिक संकेत है। 20 PPM से अधिक रीडिंग आने पर तुरंत ओवरमैन को सूचित करना और वायु नमूना लेना अनिवार्य है।"
            ),
            QuizQuestion(
                id = "1_4",
                moduleId = "1",
                question = "If an auxiliary forcing fan unexpectedly stops due to power trip in an underground blind tunnel, what must the miners inside do immediately?",
                questionHi = "यदि किसी अंधी सुरंग (ब्लाइंड हेडिंग) में वेंटिलेशन पंखा अचानक बिजली ट्रिप होने से बंद हो जाए, तो अंदर मौजूद मजदूरों को तुरंत क्या करना चाहिए?",
                options = listOf(
                    "Switch to battery-powered headlamps and continue drilling until shift ends",
                    "Immediately switch off all machinery, withdraw from the face to fresh air in the main intake drift, and fence off the entrance",
                    "Walk back and forth vigorously to stir up air currents until the fan restarts",
                    "Open compressed air valves and continue loading blasted coal"
                ),
                optionsHi = listOf(
                    "हेडलैंप चालू रखें और शिफ्ट खत्म होने तक ड्रिलिंग जारी रखें",
                    "तुरंत सभी मशीनें बंद करें, कार्यस्थल से मुख्य ताजी हवा वाले रास्ते में बाहर आएं और प्रवेश द्वार पर बाड़ लगाएं",
                    "पंखा चालू होने तक सुरंग में तेज-तेज चलकर हवा बनाने की कोशिश करें",
                    "कम्प्रेस्ड एयर का वॉल्व खोलें और कोयला लोड करते रहें"
                ),
                correctIndex = 1,
                explanation = "In a blind heading without auxiliary ventilation, methane released from freshly exposed coal quickly builds to explosive limits (5-15%). Workers must cut power, immediately withdraw to intake air, and place a danger fence across the drift entrance.",
                explanationHi = "पंखा बंद होने पर ताजे कोयले से निकलने वाली मीथेन गैस कुछ ही मिनटों में विस्फोटक स्तर तक पहुंच जाती है। इसलिए तुरंत मशीनें बंद करके मुख्य रास्ते में आना और खतरे की बाड़ लगाना अनिवार्य है।"
            )
        ),
        "2" to listOf(
            QuizQuestion(
                id = "2_1",
                moduleId = "2",
                question = "Dense black smoke is observed pouring out of the main conveyor drift. What is the very first vocational safety step every underground miner must take?",
                questionHi = "कन्वेयर बेल्ट से घना काला धुआं निकलता दिखाई देने पर, प्रत्येक भूमिगत खनिक को सबसे पहला कदम क्या उठाना चाहिए?",
                options = listOf(
                    "Run towards the fire with a water hose to extinguish it",
                    "Immediately don the chemical oxygen Self-Rescuer (SCSR) and verify the nose clamp seal before breathing mine air",
                    "Dial the surface control room from the nearest telephone and await permission to don masks",
                    "Remove safety helmet to run faster towards the shaft station"
                ),
                optionsHi = listOf(
                    "पानी का पाइप लेकर आग बुझाने के लिए उसकी तरफ दौड़ें",
                    "तुरंत अपना सेल्फ-रेस्क्यूअर पहनें और खदान की हवा में सांस लेने से पहले नाक का क्लिप कसें",
                    "नजदीकी फोन से कंट्रोल रूम को फोन करके मास्क पहनने की अनुमति मांगें",
                    "तेजी से भागने के लिए सुरक्षा हेलमेट उतार दें"
                ),
                correctIndex = 1,
                explanation = "In mine fires, the primary killer is Carbon Monoxide (CO) poisoning and anoxia. The Self-Rescuer must be deployed within 15 seconds to provide pure oxygen before taking even a single breath of toxic smoke.",
                explanationHi = "खदान की आग में सबसे जानलेवा कार्बन मोनोऑक्साइड और दम घुटना होता है। 15 सेकंड के भीतर सेल्फ-रेस्क्यूअर पहनकर शुद्ध ऑक्सीजन लेना अनिवार्य है।"
            ),
            QuizQuestion(
                id = "2_2",
                moduleId = "2",
                question = "While evacuating along a smoke-filled tunnel drift with zero visibility, which posture and technique must be maintained?",
                questionHi = "धुएं से भरी सुरंग में शून्य दृश्यता के बीच निकासी करते समय कौन सी मुद्रा और तकनीक अपनानी चाहिए?",
                options = listOf(
                    "Walk fully upright with arms waving to feel for obstacles",
                    "Stay low in a crouched crawl along the floor following illuminated green arrows or tactile guide lifeline, keeping one hand on the guide wire",
                    "Run as fast as possible in the middle of the drift between the haulage tracks",
                    "Lie flat and wait for the mine rescue brigade to locate you with infrared cameras"
                ),
                optionsHi = listOf(
                    "पूरी तरह सीधे खड़े होकर हाथ हिलाते हुए चलें",
                    "फर्श के करीब झुककर चलें और चमकते हरे तीरों व गाइड तार पर एक हाथ रखकर आगे बढ़ें",
                    "रेल की पटरियों के बीच में जितनी तेज हो सके दौड़ें",
                    "फर्श पर लेट जाएं और रेस्क्यू टीम के आने का इंतजार करें"
                ),
                correctIndex = 1,
                explanation = "Superheated smoke and lethal gases rise toward the roof due to convective buoyancy. The coolest, least toxic air and safest footing are found within 0.5 meters of the floor along the marked evacuation guide line.",
                explanationHi = "गर्म धुआं और जहरीली गैसें छत की तरफ उठती हैं। फर्श से 0.5 मीटर के दायरे में सबसे ठंडी व सुरक्षित हवा और चलने का सुरक्षित रास्ता मिलता है।"
            ),
            QuizQuestion(
                id = "2_3",
                moduleId = "2",
                question = "Upon reaching an underground Emergency Refuge Chamber / Refuge Bay, what is the mandatory airlock entry procedure?",
                questionHi = "भूमिगत आपातकालीन रिफ्यूज बे (सुरक्षित शरण कक्ष) में प्रवेश की अनिवार्य एयरलॉक प्रक्रिया क्या है?",
                options = listOf(
                    "Leave both airlock doors open to let fresh air circulate inside the chamber",
                    "Enter the outer vestibule, close and latch the outer bulkhead door, purge contaminated air with compressed air curtain, then open and bolt inner door",
                    "Take off the self-rescuer before closing the outer door",
                    "Seal all ventilation intake pipes with mud and timber wedges"
                ),
                optionsHi = listOf(
                    "दोनों दरवाजों को खुला छोड़ दें ताकि हवा अंदर आ सके",
                    "पहले बाहरी कक्ष में आएं, बाहरी दरवाजा बंद करके लॉक करें, कम्प्रेस्ड एयर से धुआं बाहर निकालें, फिर अंदर का दरवाजा खोलकर बंद करें",
                    "बाहरी दरवाजा बंद करने से पहले सेल्फ-रेस्क्यूअर उतार दें",
                    "वेंटिलेशन पाइप को मिट्टी और लकड़ी से बंद कर दें"
                ),
                correctIndex = 1,
                explanation = "Refuge bays use a two-door airlock system to prevent contaminated smoke from entering the sanctuary. The outer door must be sealed and purged before entering the main sealed shelter room.",
                explanationHi = "रिफ्यूज बे में दो दरवाजों वाला एयरलॉक होता है ताकि जहरीला धुआं मुख्य कमरे में न घुसे। बाहरी दरवाजा बंद और हवा साफ करने के बाद ही अंदर का दरवाजा खोला जाता है।"
            ),
            QuizQuestion(
                id = "2_4",
                moduleId = "2",
                question = "Why must you NEVER remove your Self-Rescuer mouthpiece to shout or speak while escaping through a smoke-filled return drift?",
                questionHi = "धुएं से भरे रास्ते से निकलते समय आवाज लगाने या बात करने के लिए सेल्फ-रेस्क्यूअर का माउथपीस मुंह से कभी क्यों नहीं निकालना चाहिए?",
                options = listOf(
                    "It will cause saliva to clog the chemical canister filter",
                    "A single inhalation of air containing 0.5% Carbon Monoxide causes rapid loss of consciousness and fatal asphyxiation within minutes",
                    "The mouthpiece cannot be re-inserted once removed",
                    "It will trigger an automatic false tamper alarm at the pit-top lamp room"
                ),
                optionsHi = listOf(
                    "इससे लार फिल्टर में चली जाएगी",
                    "हवा में 0.5% कार्बन मोनोऑक्साइड की सिर्फ एक सांस भी व्यक्ति को कुछ ही सेकंड में बेहोश और मृत कर सकती है",
                    "एक बार निकालने के बाद माउथपीस दोबारा नहीं लगाया जा सकता",
                    "इससे ऊपर लैंप रूम में अपने आप गलत अलार्म बज जाएगा"
                ),
                correctIndex = 1,
                explanation = "Carbon Monoxide binds to hemoglobin 210 times more aggressively than oxygen. Even one or two deep inhalations of CO-rich fire atmosphere can cause irreversible muscle paralysis and sudden loss of consciousness.",
                explanationHi = "कार्बन मोनोऑक्साइड खून में ऑक्सीजन से 210 गुना तेजी से घुलती है। जहरीली हवा की केवल एक या दो सांसें भी तुरंत बेहोशी और जानलेवा पक्षाघात का कारण बन जाती हैं।"
            )
        ),
        "3" to listOf(
            QuizQuestion(
                id = "3_1",
                moduleId = "3",
                question = "A large coal boulder has jammed the conveyor belt near the tail drum pulley. What is the mandatory vocational procedure before touching the conveyor?",
                questionHi = "टेल ड्रम पुली के पास कोयले का एक बड़ा पत्थर फंस गया है। कन्वेयर को छूने से पहले अनिवार्य नियम क्या है?",
                options = listOf(
                    "Have a co-worker hold the emergency trip pull-cord while you pry the rock with a crowbar",
                    "Execute full Lock-Out / Tag-Out (LOTO) on the electrical isolator, test start button for zero movement, and chock counterweight before clearing",
                    "Slow down the conveyor speed control switch and dislodge the boulder while belt creeps forward",
                    "Spray high pressure water on the drum to wash away the stuck coal while running"
                ),
                optionsHi = listOf(
                    "साथी मजदूर को पुल-कॉर्ड पकड़ने को कहें और आप सब्बल से पत्थर निकालें",
                    "इलेक्ट्रिकल आइसोलेटर पर पूर्ण लोटो (LOTO) करें, स्टार्ट बटन दबाकर शून्य गति की जांच करें और काउंटर-वेट को ब्लॉक करें",
                    "कन्वेयर की गति धीमी करके धीरे-धीरे चलते समय पत्थर निकालें",
                    "चलती बेल्ट पर तेज पानी का प्रेशर मारकर पत्थर बहा दें"
                ),
                correctIndex = 1,
                explanation = "Attempting to dislodge jammed material from running or energized machinery is the leading cause of fatal nip-point entanglements in mines. Full LOTO and zero-energy physical verification are mandatory.",
                explanationHi = "चलती या बिजली से जुड़ी मशीन से पत्थर निकालने की कोशिश खदानों में सबसे गंभीर दुर्घटनाओं का कारण बनती है। पूर्ण लोटो और शून्य ऊर्जा सत्यापन अनिवार्य है।"
            ),
            QuizQuestion(
                id = "3_2",
                moduleId = "3",
                question = "What is the primary function of the emergency pull-cord wire installed along the entire length of the coal conveyor?",
                questionHi = "कोयला कन्वेयर बेल्ट के साथ पूरी लंबाई में लगाए गए आपातकालीन पुल-कॉर्ड तार का मुख्य कार्य क्या है?",
                options = listOf(
                    "To signal telephone communication to the surface drive operator",
                    "To instantly cut power to the drive motor and stop the conveyor from any location in case of personnel entanglement or fire",
                    "To adjust belt tension dynamically during heavy coal loading",
                    "To support lighting and communication electrical cables"
                ),
                optionsHi = listOf(
                    "सतह पर बैठे ऑपरेटर से बात करने के लिए",
                    "किसी भी आपात स्थिति में किसी भी जगह से तार खींचकर तुरंत मोटर बंद करना और कन्वेयर को रोकना",
                    "कोयले के भार के अनुसार बेल्ट के तनाव को बदलना",
                    "रोशनी और टेलीफोन के तारों को सहारा देना"
                ),
                correctIndex = 1,
                explanation = "DGMS regulations mandate dual pull-wire trip switches running the full span of conveyor walkways. Pulling the wire mechanically trips the safety switch, requiring manual resetting at the switch box.",
                explanationHi = "डीजीएमएस नियमों के अनुसार यदि कोई व्यक्ति फंस जाए या दुर्घटना हो, तो कहीं से भी तार खींचने पर मोटर तुरंत बंद हो जाती है।"
            ),
            QuizQuestion(
                id = "3_3",
                moduleId = "3",
                question = "When multiple technicians (e.g., mechanical fitter and electrician) work on a conveyor system, how must padlocks be applied to the LOTO station?",
                questionHi = "जब कई कारीगर (जैसे फिटर और इलेक्ट्रीशियन) एक ही कन्वेयर पर काम करते हैं, तो लोटो स्टेशन पर ताले कैसे लगाए जाने चाहिए?",
                options = listOf(
                    "One master lock applied by the shift supervisor only",
                    "Every individual worker must place their own personal padlock on a multi-lock hasp, keeping their own key in their possession",
                    "Only the electrician applies a lock; others leave written notes on the switchgear",
                    "Padlocks are optional if the start push-button is covered with safety tape"
                ),
                optionsHi = listOf(
                    "केवल शिफ्ट सुपरवाइजर द्वारा एक ही मुख्य ताला लगाया जाएगा",
                    "प्रत्येक कारीगर मल्टी-लॉक क्लैंप पर अपना व्यक्तिगत ताला लगाएगा और चाबी अपने पास रखेगा",
                    "केवल इलेक्ट्रीशियन ताला लगाएगा, बाकी लोग केवल पर्ची चिपकाएंगे",
                    "यदि बटन पर टेप लगा हो तो ताला लगाना जरूरी नहीं है"
                ),
                correctIndex = 1,
                explanation = "The golden rule of LOTO is 'One Person, One Lock, One Key'. No worker's life should depend on someone else's padlock. The machine cannot be re-energized until every worker has safely finished and removed their own lock.",
                explanationHi = "लोटो का स्वर्णिम नियम है: 'एक व्यक्ति, एक ताला, एक चाबी'। जब तक हर कामगार अपना ताला नहीं खोल लेता, तब तक मशीन दोबारा चालू नहीं की जा सकती।"
            ),
            QuizQuestion(
                id = "3_4",
                moduleId = "3",
                question = "What is the critical danger of bypassing or removing the wire mesh guards on a rotating head drive pulley?",
                questionHi = "घूमने वाली हेड ड्राइव पुली की सुरक्षा जाली हटाने या उसे खुला छोड़ने का क्या गंभीर खतरा है?",
                options = listOf(
                    "It causes excess fine coal dust spillage onto the concrete floor",
                    "It creates an unguarded in-running nip point that can draw in clothing, limbs, or tools with irresistible mechanical force",
                    "It increases motor electrical current draw by 15%",
                    "It causes the conveyor belt tracking sensor to misalign"
                ),
                optionsHi = listOf(
                    "इससे फर्श पर ज्यादा कोयले की धूल गिरती है",
                    "यह खुला निप-प्वाइंट बनाता है जो कपड़े, हाथ या औजारों को जबर्दस्त यांत्रिक बल से तुरंत अंदर खींच लेता है",
                    "इससे मोटर 15% अधिक बिजली खर्च करने लगती है",
                    "इससे बेल्ट सेंसर गलत दिशा दिखाने लगता है"
                ),
                correctIndex = 1,
                explanation = "In-running nip points between conveyor belts and rotating drums exert tons of pull force in milliseconds. Protective fixed mesh guards prevent accidental contact and must never be defeated or left unbolted.",
                explanationHi = "बेल्ट और पुली के बीच का निप-प्वाइंट कुछ ही मिलीसेकंड में कई टन खिंचाव बल उत्पन्न करता है, जिससे जानलेवा खिंचाव हो सकता है। सुरक्षा जाली हमेशा कसी होनी चाहिए।"
            )
        ),
        "4" to listOf(
            QuizQuestion(
                id = "4_1",
                moduleId = "4",
                question = "Before opening the flanged door of an underground Flameproof (FLP) 3.3kV transformer switchgear, what sequence must be executed?",
                questionHi = "भूमिगत 3.3 केवी फ्लेमप्रूफ सबस्टेशन के दरवाजे के बोल्ट खोलने से पहले कौन सा सही क्रम अपनाना अनिवार्य है?",
                options = listOf(
                    "Loosen bolts halfway, then switch off isolator handle",
                    "Open upstream breaker, verify zero voltage with rated probe, engage earth grounding knife switch, then open enclosure bolts",
                    "Blow compressed air into enclosure ventilation vents to cool busbars",
                    "Ensure rubber boots are worn and touch busbars quickly with back of hand"
                ),
                optionsHi = listOf(
                    "बोल्ट आधे ढीले करें, फिर आइसोलेटर बंद करें",
                    "मुख्य ब्रेकर बंद करें, रेटेड टेस्टर से शून्य वोल्टेज जांचें, अर्थ नाइफ स्विच से ग्राउंडिंग करें, फिर बोल्ट खोलें",
                    "कम्प्रेस्ड एयर मारकर बसबार को ठंडा करें",
                    "रबर के जूते पहनकर हाथ के पिछले हिस्से से छूकर करंट देखें"
                ),
                correctIndex = 1,
                explanation = "Flameproof enclosures contain explosive pressure. Opening live or ungrounded high-voltage gear underground can trigger catastrophic arc flash explosions and methane ignition. Verify zero potential and ground busbars first.",
                explanationHi = "फ्लेमप्रूफ बॉक्स में बहुत उच्च वोल्टेज होता है। बिना बिजली काटे और बिना अर्थिंग किए खोलने पर जानलेवा आर्क फ्लैश विस्फोट और मीथेन गैस में आग लग सकती है।"
            ),
            QuizQuestion(
                id = "4_2",
                moduleId = "4",
                question = "An underground feeder Earth Leakage Relay (ELR) trips repeatedly on a mobile coal-cutting machine. What is the correct vocational action?",
                questionHi = "कोयला काटने वाली मशीन का अर्थ लीकेज रिले (ELR) बार-बार ट्रिप हो रहा है। सही व्यावसायिक कार्रवाई क्या है?",
                options = listOf(
                    "Increase the trip current threshold from 750mA to 2A to prevent nuisance trips",
                    "Thoroughly inspect the trailing cable for puncture damage, moisture ingress, and phase-to-earth leakage before resetting the relay",
                    "Bridge the earth leakage contact with copper jumper wire to maintain production",
                    "Reset the circuit breaker repeatedly until it holds power"
                ),
                optionsHi = listOf(
                    "ट्रिप करंट को 750mA से बढ़ाकर 2A कर दें ताकि बार-बार बंद न हो",
                    "रिले रीसेट करने से पहले केबल के कटने, पानी घुसने या अर्थ फॉल्ट की पूरी जांच करें",
                    "उत्पादन चालू रखने के लिए तांबे के तार से रिले को बाईपास कर दें",
                    "सर्किट ब्रेकर को बार-बार चालू करते रहें जब तक वह टिक न जाए"
                ),
                correctIndex = 1,
                explanation = "Bridging or defeating earth leakage relays is a criminal violation under CEA and DGMS rules. Repeated trips signify broken cable insulation or water entry, creating severe electrocution and fire hazards.",
                explanationHi = "अर्थ लीकेज रिले को बाईपास करना एक गंभीर गैर-कानूनी अपराध है। बार-बार ट्रिप होना यह दर्शाता है कि केबल कटी है या उसमें पानी घुस गया है, जिससे करंट लगने का भारी खतरा है।"
            ),
            QuizQuestion(
                id = "4_3",
                moduleId = "4",
                question = "What generates an Arc Flash blast inside a mining electrical substation, and why is it so deadly?",
                questionHi = "इलेक्ट्रिकल सबस्टेशन में आर्क फ्लैश ब्लास्ट क्यों होता है और यह इतना जानलेवा क्यों है?",
                options = listOf(
                    "Battery acid boiling over inside the cap lamp rack",
                    "A low-impedance electrical fault ionizing air into plasma (>19,000°C), producing supersonic pressure waves, molten copper shrapnel, and intense radiant heat",
                    "Over-torquing the enclosure flanged bolts with an impact wrench",
                    "Nitrogen gas escaping from hydraulic suspension cylinders"
                ),
                optionsHi = listOf(
                    "कैप लैंप की बैटरी से तेजाब उबलना",
                    "विद्युत शॉर्ट सर्किट से हवा का 19,000 डिग्री से अधिक तापमान वाले प्लाज्मा में बदलना, जिससे अत्यधिक दबाव, पिघला तांबा और तीव्र उष्मा निकलती है",
                    "इंपैक्ट रिंच से बोल्ट को ज्यादा कस देना",
                    "हाइड्रोलिक सिलेंडर से नाइट्रोजन गैस का रिसाव"
                ),
                correctIndex = 1,
                explanation = "An electric arc flash generates temperatures four times hotter than the surface of the sun. The resulting plasma explosion vaporizes copper conductors instantly, expanding 67,000-fold and causing fatal burns and concussive blast trauma.",
                explanationHi = "आर्क फ्लैश सूरज की सतह से चार गुना अधिक तापमान पैदा करता है। इसमें तांबा वाष्प बनकर हजारों गुना फैलता है, जिससे गंभीर जलने और विस्फोट का झटका लगता है।"
            ),
            QuizQuestion(
                id = "4_4",
                moduleId = "4",
                question = "Why must high-voltage flameproof (FLP) enclosure flanged joint surfaces be kept mirror-clean and never painted or scarred?",
                questionHi = "हाई-वोल्टेज फ्लेमप्रूफ (FLP) बॉक्स के किनारों के जोड़ों को बिल्कुल साफ क्यों रखना चाहिए और उन पर पेंट क्यों नहीं होना चाहिए?",
                options = listOf(
                    "Paint makes the switchgear look untidy during DGMS official inspections",
                    "The precisely machined flanged gap quenches internal flame gases so escaping combustion cannot ignite surrounding methane in the mine atmosphere",
                    "Paint absorbs electric current and causes stray voltage on the outer cabinet",
                    "It prevents the steel enclosure from rusting in humid mine atmospheres"
                ),
                optionsHi = listOf(
                    "पेंट लगाने से आधिकारिक निरीक्षण के समय बॉक्स गंदा दिखता है",
                    "बारीक जोड़ों का सटीक अंतर अंदर की चिंगारी को ठंडा करके बुझा देता है ताकि बाहर की मीथेन गैस में आग न लगे",
                    "पेंट करंट सोख लेता है जिससे बाहरी बक्से में करंट आ जाता है",
                    "यह बक्से को खदान की नमी में जंग लगने से बचाता है"
                ),
                correctIndex = 1,
                explanation = "The fundamental engineering principle of Flameproof (Ex 'd') apparatus is that any internal gas explosion is cooled and quenched as it escapes through the narrow machined gap (0.4mm), preventing ignition of ambient methane.",
                explanationHi = "फ्लेमप्रूफ बक्से का सिद्धांत यह है कि यदि बक्से के भीतर कोई चिंगारी या विस्फोट हो, तो जोड़ों के बारीक अंतर से गुजरते समय वह ठंडी हो जाए और खदान की हवा में आग न भड़क सके।"
            )
        ),
        "5" to listOf(
            QuizQuestion(
                id = "5_1",
                moduleId = "5",
                question = "During pre-shift muster inspection, what indicator on the chemical oxygen Self-Rescuer confirms that its hermetic seal is intact?",
                questionHi = "ड्यूटी शुरू होने से पहले मस्टर जांच के दौरान सेल्फ-रेस्क्यूअर की सील सुरक्षित होने की पुष्टि कैसे होती है?",
                options = listOf(
                    "The serial number printed on the plastic holster",
                    "The transparent moisture indicator window displaying blue/green dry crystals, and the lead wire tamper seal unbroken",
                    "The weight stamped on the bottom of the steel bracket",
                    "The presence of a rubber smell when smelling the canister seams"
                ),
                optionsHi = listOf(
                    "प्लास्टिक कवर पर लिखा हुआ सीरियल नंबर देखकर",
                    "नमी सूचक खिड़की में सूखे नीले/हरे क्रिस्टल दिखना और लेड सील का तार पूरी तरह अटूट होना",
                    "स्टील ब्रैकेट पर मुद्रित वजन देखकर",
                    "कैनीस्टर को सूंघकर रबर की गंध देखना"
                ),
                correctIndex = 1,
                explanation = "Chemical oxygen canisters contain potassium superoxide (KO2) or chlorate candles that react vigorously with ambient moisture. If the seal fails, crystals turn pink/white, rendering the device incapable of producing life-saving oxygen.",
                explanationHi = "सेल्फ-रेस्क्यूअर में रासायनिक ऑक्सीजन होती है जो नमी से खराब हो जाती है। यदि सील टूटी हो तो क्रिस्टल गुलाबी या सफेद हो जाते हैं, जिससे यह जीवनरक्षक उपकरण बेकार हो जाता है।"
            ),
            QuizQuestion(
                id = "5_2",
                moduleId = "5",
                question = "Why are antistatic, steel-toed boots mandatory for all underground personnel under DGMS Mine Vocational Rules?",
                questionHi = "डीजीएमएस नियमों के अनुसार भूमिगत खदान में काम करने वाले सभी कर्मियों के लिए एंटी-स्टैटिक स्टील-टो जूते क्यों अनिवार्य हैं?",
                options = listOf(
                    "To prevent workers from slipping on slick conveyor belt oil and to prevent static spark build-up that could trigger methane/coal dust explosions",
                    "To absorb vibrational fatigue while walking on railway ballast tracks",
                    "To keep miner feet dry while wading through pit-bottom drainage sumps",
                    "To provide insulation against freezing winter temperatures in intake shafts"
                ),
                optionsHi = listOf(
                    "पैरों की सुरक्षा के साथ-साथ शरीर में स्थैतिक बिजली का स्पार्क बनने से रोकना ताकि मीथेन गैस में आग न लगे",
                    "रेलवे ट्रैक पर चलते समय पैरों की थकान को कम करने के लिए",
                    "पानी के गड्ढों में चलते समय पैरों को सूखा रखने के लिए",
                    "सर्दियों में ठंड से बचाने के लिए"
                ),
                correctIndex = 0,
                explanation = "Standard rubber or plastic soles build up electrostatic charges of over 10,000 volts when walking. Antistatic soles safely bleed static charges to earth, eliminating dangerous electrostatic sparks in gassy coal seams.",
                explanationHi = "सामान्य जूतों में चलने से हजारों वोल्ट का स्थैतिक आवेश बन सकता है जो मीथेन गैस में विस्फोट करा सकता है। एंटी-स्टैटिक जूते इस आवेश को सुरक्षित रूप से जमीन में विसर्जित कर देते हैं।"
            ),
            QuizQuestion(
                id = "5_3",
                moduleId = "5",
                question = "Under the Mines Act 1952, which of the following is strictly classified as CONTRABAND and prohibited past the pit-head security check?",
                questionHi = "खान अधिनियम 1952 के तहत, सुरक्षा जांच पर किसे प्रतिबंधित सामग्री मानकर खदान में ले जाना सख्त मना है?",
                options = listOf(
                    "Non-intrinsically safe smartphones, matchboxes, cigarette lighters, and non-certified battery devices",
                    "Clean drinking water bottles and prescription spectacles",
                    "Brass identification tokens and DGMS statutory logbooks",
                    "Standard cotton mining towels and leather gloves"
                ),
                optionsHi = listOf(
                    "सामान्य स्मार्टफोन, माचिस, सिगरेट लाइटर, बीड़ी और अप्रमाणित बैटरी उपकरण",
                    "पीने के पानी की बोतल और नजर का चश्मा",
                    "पीतल का पहचान टोकन और वैधानिक लॉगबुक",
                    "सूती तौलिया और चमड़े के सुरक्षा दस्ताने"
                ),
                correctIndex = 0,
                explanation = "Underground coal mines are classified as hazardous explosive atmospheres. Non-flameproof mobile phones, lighters, and ordinary battery electronics can generate ignition sparks and are strictly prohibited contraband.",
                explanationHi = "खदान में सामान्य मोबाइल, माचिस या गैर-प्रमाणित बैटरी से निकलने वाली हल्की सी चिंगारी भी भयानक विस्फोट कर सकती है। इन्हें सुरक्षा गेट पर जमा करना अनिवार्य है।"
            ),
            QuizQuestion(
                id = "5_4",
                moduleId = "5",
                question = "How frequently must a miner's cap lamp provide continuous illumination, and what check must be done before boarding the shaft cage?",
                questionHi = "माइनर की कैप लैंप को लगातार कितने घंटे रोशनी देनी चाहिए, और केज में चढ़ने से पहले क्या जांच आवश्यक है?",
                options = listOf(
                    "Minimum 4 hours; test by shining at the pit office ceiling",
                    "Minimum 12 continuous hours at certified lux output; inspect cable strain relief, switch lock, and confirm green full-charge status",
                    "Minimum 24 hours; verify by shaking the lithium battery pack",
                    "Minimum 2 hours with emergency flashing beacon only"
                ),
                optionsHi = listOf(
                    "कम से कम 4 घंटे; छत की तरफ जलाकर जांचें",
                    "कम से कम 12 घंटे लगातार तेज रोशनी; केबल, स्विच लॉक और फुल चार्जिंग की हरी बत्ती की पुष्टि करें",
                    "कम से कम 24 घंटे; बैटरी को हिलाकर देखें",
                    "कम से कम 2 घंटे केवल फ्लैशिंग मोड में"
                ),
                correctIndex = 1,
                explanation = "A miner's cap lamp is their primary lifeline in total underground darkness. DGMS standards mandate a minimum 12-hour continuous burn duration. Damaged cables or loose bezels can cause fatal underground light failure.",
                explanationHi = "घोर अंधेरे में कैप लैंप ही मजदूर का सबसे बड़ा सहारा होती है। डीजीएमएस के अनुसार इसे कम से कम 12 घंटे लगातार तेज रोशनी देनी चाहिए।"
            )
        )
    )
}
