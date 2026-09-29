package com.example.ui

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

enum class ARSimulationType {
    GAS_VENTILATION,
    FIRE_EVACUATION,
    MACHINERY_GUARDING,
    ELECTRICAL_ISOLATION,
    PPE_INSPECTION
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ThreeDEnvironmentView(
    simulationType: ARSimulationType,
    onHazardIdentified: (type: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val simTypeStr = when (simulationType) {
        ARSimulationType.GAS_VENTILATION -> "gas"
        ARSimulationType.FIRE_EVACUATION -> "fire"
        ARSimulationType.MACHINERY_GUARDING -> "machinery"
        ARSimulationType.ELECTRICAL_ISOLATION -> "electrical"
        ARSimulationType.PPE_INSPECTION -> "ppe"
    }

    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
            <style>
                body {
                    margin: 0;
                    overflow: hidden;
                    background-color: transparent;
                    user-select: none;
                    -webkit-user-select: none;
                }
                canvas {
                    width: 100vw;
                    height: 100vh;
                    display: block;
                }
            </style>
            <script src="https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js"></script>
        </head>
        <body>
            <script>
                const SIM_TYPE = '$simTypeStr';
                let scene, camera, renderer, rig;
                let fumesGroup, fireGroup, arrowsGroup, exitGroup, machineGroup, electricalGroup, ppeGroup;
                let gasInteractiveMesh, flameLight, alarmStrobeLight, sparkLight;
                let conveyorBeltMesh, rotatingRollers = [], drivePinchMesh, lotoStationMesh;
                let electricalSparks = [], isolatorHandleMesh, busbars = [];
                let ppeWorkbenchGroup, selfRescuerMesh, capLampMesh, helmetMesh, gasDetectorMesh, ppeTargetRing;
                let isMachineLockedOut = false;
                let isCircuitIsolated = false;
                let isPpeInspected = false;
                let rotY = 0, rotX = 0;
                let isDown = false, lastX = 0, lastY = 0;
                let clock = new THREE.Clock();

                function init() {
                    scene = new THREE.Scene();
                    camera = new THREE.PerspectiveCamera(55, window.innerWidth / window.innerHeight, 0.1, 100);
                    camera.position.set(0, 1.8, 8);

                    renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
                    renderer.setSize(window.innerWidth, window.innerHeight);
                    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
                    renderer.setClearColor(0x000000, 0);
                    document.body.appendChild(renderer.domElement);

                    // Base Ambient & Directional Lighting
                    const ambient = new THREE.AmbientLight(0x454c55, 1.1);
                    scene.add(ambient);

                    const headlamp = new THREE.SpotLight(0xfff5e0, 1.9, 40, Math.PI / 3.8, 0.45);
                    headlamp.position.set(0, 3.2, 7.5);
                    scene.add(headlamp);

                    rig = new THREE.Group();
                    scene.add(rig);

                    // Build Common Mine Drift Tunnel Architecture
                    buildMineTunnel();

                    // Load specific 3D AR Model based on simulation type
                    if (SIM_TYPE === 'gas') {
                        setupGasLeakSimulation();
                    } else if (SIM_TYPE === 'fire') {
                        setupFireEvacuationSimulation();
                    } else if (SIM_TYPE === 'machinery') {
                        setupMachineryGuardingSimulation();
                    } else if (SIM_TYPE === 'electrical') {
                        setupElectricalIsolationSimulation();
                    } else if (SIM_TYPE === 'ppe') {
                        setupPpeInspectionSimulation();
                    }

                    setupTouchControls();
                    animate();

                    window.addEventListener('resize', onWindowResize);
                }

                function buildMineTunnel() {
                    const archMat = new THREE.MeshStandardMaterial({
                        color: 0x24282c,
                        roughness: 0.85,
                        metalness: 0.35
                    });

                    // Steel Arch Canopy Ribs
                    for (let i = 0; i < 9; i++) {
                        const archGeo = new THREE.TorusGeometry(3.3, 0.16, 8, 24, Math.PI);
                        const arch = new THREE.Mesh(archGeo, archMat);
                        arch.rotation.z = Math.PI;
                        arch.position.set(0, -0.2, -i * 3.2 + 5);
                        rig.add(arch);

                        // Vertical support stanchions
                        [-3.1, 3.1].forEach(x => {
                            const postGeo = new THREE.BoxGeometry(0.16, 2.8, 0.16);
                            const post = new THREE.Mesh(postGeo, archMat);
                            post.position.set(x, -1.6, -i * 3.2 + 5);
                            rig.add(post);
                        });
                    }

                    // Rock Floor with subtle underground ballast texture
                    const floorGeo = new THREE.PlaneGeometry(6.6, 34);
                    const floorMat = new THREE.MeshStandardMaterial({
                        color: 0x16191c,
                        roughness: 0.95,
                        transparent: true,
                        opacity: 0.82
                    });
                    const floor = new THREE.Mesh(floorGeo, floorMat);
                    floor.rotation.x = -Math.PI / 2;
                    floor.position.set(0, -3.0, -8);
                    rig.add(floor);

                    // Utility Pipe along tunnel crown
                    const pipeGeo = new THREE.CylinderGeometry(0.12, 0.12, 34, 16);
                    const pipeMat = new THREE.MeshStandardMaterial({ color: 0x6e3d1c, metalness: 0.5, roughness: 0.4 });
                    const pipe = new THREE.Mesh(pipeGeo, pipeMat);
                    pipe.rotation.x = Math.PI / 2;
                    pipe.position.set(-1.6, 2.7, -8);
                    rig.add(pipe);
                }

                // ================= 1. GAS LEAKAGE & VENTILATION =================
                function setupGasLeakSimulation() {
                    fumesGroup = new THREE.Group();
                    rig.add(fumesGroup);

                    const crackGeo = new THREE.BoxGeometry(0.3, 0.3, 0.5);
                    const crackMat = new THREE.MeshStandardMaterial({
                        color: 0x331000,
                        emissive: 0xff4500,
                        emissiveIntensity: 0.7
                    });
                    const crackBox = new THREE.Mesh(crackGeo, crackMat);
                    crackBox.position.set(-1.6, 2.7, -2.5);
                    fumesGroup.add(crackBox);

                    const valveGeo = new THREE.CylinderGeometry(0.35, 0.35, 0.12, 16);
                    const valveMat = new THREE.MeshStandardMaterial({
                        color: 0xffaa00,
                        emissive: 0xff8800,
                        emissiveIntensity: 1.2
                    });
                    gasInteractiveMesh = new THREE.Mesh(valveGeo, valveMat);
                    gasInteractiveMesh.rotation.z = Math.PI / 2;
                    gasInteractiveMesh.position.set(-1.3, 2.7, -2.5);
                    fumesGroup.add(gasInteractiveMesh);

                    const ringGeo = new THREE.RingGeometry(0.48, 0.58, 32);
                    const ringMat = new THREE.MeshBasicMaterial({
                        color: 0xffcc00,
                        side: THREE.DoubleSide,
                        transparent: true,
                        opacity: 0.85
                    });
                    const targetRing = new THREE.Mesh(ringGeo, ringMat);
                    targetRing.position.set(-1.25, 2.7, -2.5);
                    targetRing.rotation.y = Math.PI / 2;
                    fumesGroup.add(targetRing);
                    fumesGroup.targetRing = targetRing;

                    // Volumetric Billowy Fumes
                    const fumesCount = 38;
                    fumesGroup.particles = [];
                    const fumeMat = new THREE.MeshStandardMaterial({
                        color: 0x88cc22,
                        emissive: 0x448811,
                        emissiveIntensity: 0.5,
                        transparent: true,
                        opacity: 0.45,
                        roughness: 1.0
                    });

                    for (let i = 0; i < fumesCount; i++) {
                        const radius = 0.22 + Math.random() * 0.28;
                        const pGeo = new THREE.SphereGeometry(radius, 10, 10);
                        const particle = new THREE.Mesh(pGeo, fumeMat.clone());
                        resetFumeParticle(particle, true);
                        fumesGroup.add(particle);
                        fumesGroup.particles.push(particle);
                    }

                    const gasLight = new THREE.PointLight(0xccff33, 1.5, 9);
                    gasLight.position.set(-1.5, 2.5, -2.5);
                    fumesGroup.add(gasLight);
                }

                function resetFumeParticle(p, init = false) {
                    p.position.set(
                        -1.6 + (Math.random() - 0.5) * 0.35,
                        2.6 + (Math.random() - 0.5) * 0.2,
                        -2.5 + (Math.random() - 0.5) * 0.35
                    );
                    if (init) {
                        p.position.y += Math.random() * 1.5;
                        p.position.x += (Math.random() - 0.5) * 1.2;
                        p.position.z += (Math.random() - 0.5) * 1.5;
                    }
                    p.scale.setScalar(0.4 + Math.random() * 0.4);
                    p.userData = {
                        vx: (Math.random() - 0.35) * 0.018,
                        vy: -0.012 - Math.random() * 0.016,
                        vz: (Math.random() - 0.5) * 0.02,
                        grow: 0.015 + Math.random() * 0.012,
                        life: Math.random() * 1.0
                    };
                }

                // ================= 2. FIRE & RUNWAY EVACUATION =================
                function setupFireEvacuationSimulation() {
                    fireGroup = new THREE.Group();
                    rig.add(fireGroup);
                    arrowsGroup = new THREE.Group();
                    rig.add(arrowsGroup);
                    exitGroup = new THREE.Group();
                    rig.add(exitGroup);

                    const rubbleGeo = new THREE.BoxGeometry(1.6, 0.7, 2.2);
                    const rubbleMat = new THREE.MeshStandardMaterial({ color: 0x1e1510, roughness: 0.9 });
                    const rubble = new THREE.Mesh(rubbleGeo, rubbleMat);
                    rubble.position.set(-1.8, -2.65, -2.2);
                    fireGroup.add(rubble);

                    const flameCount = 32;
                    fireGroup.flames = [];
                    const flameGeo = new THREE.ConeGeometry(0.35, 1.2, 8);
                    const flameMat1 = new THREE.MeshStandardMaterial({
                        color: 0xff3300,
                        emissive: 0xff6600,
                        emissiveIntensity: 2.2,
                        roughness: 0.3
                    });
                    const flameMat2 = new THREE.MeshStandardMaterial({
                        color: 0xffaa00,
                        emissive: 0xffdd22,
                        emissiveIntensity: 2.8,
                        roughness: 0.2
                    });

                    for (let i = 0; i < flameCount; i++) {
                        const mat = (i % 2 === 0) ? flameMat1.clone() : flameMat2.clone();
                        const flame = new THREE.Mesh(flameGeo, mat);
                        const xOffset = -1.8 + (Math.random() - 0.5) * 1.4;
                        const zOffset = -2.2 + (Math.random() - 0.5) * 1.8;
                        const yOffset = -2.3 + Math.random() * 0.4;
                        flame.position.set(xOffset, yOffset, zOffset);
                        const scale = 0.5 + Math.random() * 0.8;
                        flame.scale.set(scale, scale * 1.4, scale);
                        flame.userData = {
                            baseY: yOffset,
                            speed: 6 + Math.random() * 8,
                            phase: Math.random() * Math.PI * 2
                        };
                        fireGroup.add(flame);
                        fireGroup.flames.push(flame);
                    }

                    flameLight = new THREE.PointLight(0xff7711, 2.8, 14);
                    flameLight.position.set(-1.8, -1.0, -2.2);
                    fireGroup.add(flameLight);

                    // Real-Time Animated Floor Chevron Arrows
                    arrowsGroup.arrows = [];
                    const arrowShape = new THREE.Shape();
                    arrowShape.moveTo(0, 0.45);
                    arrowShape.lineTo(0.35, 0.1);
                    arrowShape.lineTo(0.2, 0.1);
                    arrowShape.lineTo(0.2, -0.45);
                    arrowShape.lineTo(-0.2, -0.45);
                    arrowShape.lineTo(-0.2, 0.1);
                    arrowShape.lineTo(-0.35, 0.1);
                    arrowShape.closePath();

                    const extrudeSettings = { depth: 0.04, bevelEnabled: true, bevelSegments: 2, steps: 1, bevelSize: 0.02, bevelThickness: 0.02 };
                    const arrowGeo = new THREE.ExtrudeGeometry(arrowShape, extrudeSettings);

                    const waypoints = [
                        { x: 1.2, z: 3.5 },
                        { x: 1.3, z: 1.8 },
                        { x: 1.4, z: 0.0 },
                        { x: 1.4, z: -1.8 },
                        { x: 1.2, z: -3.6 },
                        { x: 0.9, z: -5.4 },
                        { x: 0.3, z: -7.2 },
                        { x: 0.0, z: -9.0 }
                    ];

                    waypoints.forEach((wp, idx) => {
                        const arrowMat = new THREE.MeshStandardMaterial({
                            color: 0x00ff88,
                            emissive: 0x00cc66,
                            emissiveIntensity: 1.2,
                            roughness: 0.4
                        });
                        const arrowMesh = new THREE.Mesh(arrowGeo, arrowMat);
                        arrowMesh.rotation.x = -Math.PI / 2;
                        arrowMesh.position.set(wp.x, -2.96, wp.z);
                        arrowsGroup.add(arrowMesh);
                        arrowsGroup.arrows.push(arrowMesh);
                    });

                    // Refuge Bay Doorway with green beacon
                    const doorFrameGeo = new THREE.BoxGeometry(2.4, 3.2, 0.3);
                    const doorFrameMat = new THREE.MeshStandardMaterial({ color: 0x334433, metalness: 0.6, roughness: 0.4 });
                    const doorFrame = new THREE.Mesh(doorFrameGeo, doorFrameMat);
                    doorFrame.position.set(0, -1.4, -9.5);
                    exitGroup.add(doorFrame);

                    const signGeo = new THREE.BoxGeometry(1.6, 0.5, 0.1);
                    const signMat = new THREE.MeshStandardMaterial({
                        color: 0x00ff66,
                        emissive: 0x00cc44,
                        emissiveIntensity: 1.5
                    });
                    const signMesh = new THREE.Mesh(signGeo, signMat);
                    signMesh.position.set(0, 0.6, -9.3);
                    exitGroup.add(signMesh);

                    const beaconGeo = new THREE.SphereGeometry(0.18, 12, 12);
                    const beaconMat = new THREE.MeshStandardMaterial({
                        color: 0x00ffaa,
                        emissive: 0x00ff88,
                        emissiveIntensity: 2.5
                    });
                    const beacon = new THREE.Mesh(beaconGeo, beaconMat);
                    beacon.position.set(0, 1.1, -9.3);
                    exitGroup.add(beacon);

                    alarmStrobeLight = new THREE.PointLight(0x00ff88, 2.5, 15);
                    alarmStrobeLight.position.set(0, 1.0, -9.0);
                    exitGroup.add(alarmStrobeLight);
                }

                // ================= 3. CONVEYOR & MACHINE GUARDING (LOTO) =================
                function setupMachineryGuardingSimulation() {
                    machineGroup = new THREE.Group();
                    rig.add(machineGroup);

                    // Heavy Steel Conveyor Frame (Running along right side)
                    const frameMat = new THREE.MeshStandardMaterial({ color: 0x2c3e50, metalness: 0.65, roughness: 0.5 });
                    const guardHazardMat = new THREE.MeshStandardMaterial({
                        color: 0xf1c40f,
                        emissive: 0xd68910,
                        emissiveIntensity: 0.8,
                        roughness: 0.3
                    });

                    // Side stringer beams
                    for (let i = 0; i < 4; i++) {
                        const legGeo = new THREE.BoxGeometry(0.14, 1.6, 0.14);
                        const legLeft = new THREE.Mesh(legGeo, frameMat);
                        legLeft.position.set(0.6, -2.2, -i * 2.8 + 2);
                        const legRight = new THREE.Mesh(legGeo, frameMat);
                        legRight.position.set(2.2, -2.2, -i * 2.8 + 2);
                        machineGroup.add(legLeft);
                        machineGroup.add(legRight);

                        const crossGeo = new THREE.BoxGeometry(1.7, 0.1, 0.1);
                        const crossBar = new THREE.Mesh(crossGeo, frameMat);
                        crossBar.position.set(1.4, -1.8, -i * 2.8 + 2);
                        machineGroup.add(crossBar);
                    }

                    // Conveyor Belt Bed & Coal Carrier
                    const beltGeo = new THREE.BoxGeometry(1.4, 0.08, 12);
                    const beltMat = new THREE.MeshStandardMaterial({ color: 0x111111, roughness: 0.9 });
                    conveyorBeltMesh = new THREE.Mesh(beltGeo, beltMat);
                    conveyorBeltMesh.position.set(1.4, -1.35, -2.0);
                    machineGroup.add(conveyorBeltMesh);

                    // Rotating Roller Idlers
                    const rollerGeo = new THREE.CylinderGeometry(0.14, 0.14, 1.45, 16);
                    const rollerMat = new THREE.MeshStandardMaterial({ color: 0x7f8c8d, metalness: 0.8, roughness: 0.2 });
                    for (let z = 3; z >= -7; z -= 1.8) {
                        const roller = new THREE.Mesh(rollerGeo, rollerMat);
                        roller.rotation.z = Math.PI / 2;
                        roller.position.set(1.4, -1.45, z);
                        machineGroup.add(roller);
                        rotatingRollers.push(roller);
                    }

                    // EXPOSED PINCH-POINT DRIVE HEAD DRUM (HAZARD)
                    const driveDrumGeo = new THREE.CylinderGeometry(0.42, 0.42, 1.5, 20);
                    drivePinchMesh = new THREE.Mesh(driveDrumGeo, guardHazardMat);
                    drivePinchMesh.rotation.z = Math.PI / 2;
                    drivePinchMesh.position.set(1.4, -1.35, 3.4);
                    machineGroup.add(drivePinchMesh);

                    // Hazard Warning Ring around pinch nip-point
                    const hazardRingGeo = new THREE.TorusGeometry(0.58, 0.06, 8, 24);
                    const hazardRingMat = new THREE.MeshBasicMaterial({ color: 0xff2200 });
                    const hazardRing = new THREE.Mesh(hazardRingGeo, hazardRingMat);
                    hazardRing.rotation.y = Math.PI / 2;
                    hazardRing.position.set(0.6, -1.35, 3.4);
                    machineGroup.add(hazardRing);
                    machineGroup.hazardRing = hazardRing;

                    // Emergency Trip-Wire (Red cable running along walkway edge)
                    const wireGeo = new THREE.CylinderGeometry(0.02, 0.02, 12, 8);
                    const wireMat = new THREE.MeshStandardMaterial({ color: 0xe74c3c, emissive: 0xc0392b, emissiveIntensity: 0.9 });
                    const tripWire = new THREE.Mesh(wireGeo, wireMat);
                    tripWire.rotation.x = Math.PI / 2;
                    tripWire.position.set(0.45, -1.1, -2.0);
                    machineGroup.add(tripWire);

                    // LOCK-OUT / TAG-OUT (LOTO) STATION & ISOLATOR BOX (Interactive)
                    const lotoBoxGeo = new THREE.BoxGeometry(0.5, 0.7, 0.3);
                    const lotoBoxMat = new THREE.MeshStandardMaterial({
                        color: 0xc0392b,
                        emissive: 0x962d22,
                        emissiveIntensity: 0.8,
                        roughness: 0.4
                    });
                    lotoStationMesh = new THREE.Mesh(lotoBoxGeo, lotoBoxMat);
                    lotoStationMesh.position.set(-0.3, -1.0, 1.5);
                    machineGroup.add(lotoStationMesh);

                    // Red Padlock & Yellow Danger Tag on LOTO Station
                    const padlockGeo = new THREE.TorusGeometry(0.12, 0.03, 8, 16, Math.PI);
                    const padlockMat = new THREE.MeshStandardMaterial({ color: 0xf39c12, metalness: 0.8 });
                    const padlock = new THREE.Mesh(padlockGeo, padlockMat);
                    padlock.position.set(-0.3, -0.6, 1.66);
                    machineGroup.add(padlock);

                    const tagGeo = new THREE.PlaneGeometry(0.22, 0.35);
                    const tagMat = new THREE.MeshBasicMaterial({ color: 0xffffff, side: THREE.DoubleSide });
                    const dangerTag = new THREE.Mesh(tagGeo, tagMat);
                    dangerTag.position.set(-0.3, -0.85, 1.68);
                    machineGroup.add(dangerTag);

                    // Pulsing Ring around LOTO Station
                    const lotoRingGeo = new THREE.RingGeometry(0.45, 0.55, 24);
                    const lotoRingMat = new THREE.MeshBasicMaterial({ color: 0x00ffcc, side: THREE.DoubleSide, transparent: true, opacity: 0.9 });
                    const lotoRing = new THREE.Mesh(lotoRingGeo, lotoRingMat);
                    lotoRing.position.set(-0.3, -1.0, 1.7);
                    machineGroup.add(lotoRing);
                    machineGroup.lotoRing = lotoRing;

                    const machineLight = new THREE.PointLight(0xffa500, 1.4, 7);
                    machineLight.position.set(1.4, -0.5, 3.0);
                    machineGroup.add(machineLight);
                }

                // ================= 4. ELECTRICAL ISOLATION & ARC FLASH =================
                function setupElectricalIsolationSimulation() {
                    electricalGroup = new THREE.Group();
                    rig.add(electricalGroup);

                    // Explosion-Proof Flameproof Heavy Cast Enclosure (Substation Cubicle)
                    const cubicleGeo = new THREE.BoxGeometry(2.0, 2.8, 1.0);
                    const cubicleMat = new THREE.MeshStandardMaterial({
                        color: 0x2c3437,
                        metalness: 0.75,
                        roughness: 0.45
                    });
                    const cubicle = new THREE.Mesh(cubicleGeo, cubicleMat);
                    cubicle.position.set(0, -1.0, -1.5);
                    electricalGroup.add(cubicle);

                    // High Voltage Warning Triangle Emblem
                    const triShape = new THREE.Shape();
                    triShape.moveTo(0, 0.4);
                    triShape.lineTo(0.35, -0.3);
                    triShape.lineTo(-0.35, -0.3);
                    triShape.closePath();
                    const triGeo = new THREE.ShapeGeometry(triShape);
                    const triMat = new THREE.MeshBasicMaterial({ color: 0xffcc00, side: THREE.DoubleSide });
                    const warningSign = new THREE.Mesh(triGeo, triMat);
                    warningSign.position.set(0, 0.05, -0.98);
                    electricalGroup.add(warningSign);

                    // Open Maintenance Bay showing 3-Phase High Voltage Busbars
                    const bayCutGeo = new THREE.BoxGeometry(1.4, 1.1, 0.4);
                    const bayCutMat = new THREE.MeshStandardMaterial({ color: 0x0a0c0e, roughness: 0.9 });
                    const bayInterior = new THREE.Mesh(bayCutGeo, bayCutMat);
                    bayInterior.position.set(0, -1.3, -0.9);
                    electricalGroup.add(bayInterior);

                    // 3 Copper Busbars (Red, Yellow, Blue phases)
                    const phaseColors = [0xdd3322, 0xeebb11, 0x2266dd];
                    [-0.38, 0, 0.38].forEach((x, idx) => {
                        const barGeo = new THREE.BoxGeometry(0.08, 0.85, 0.05);
                        const barMat = new THREE.MeshStandardMaterial({
                            color: phaseColors[idx],
                            metalness: 0.9,
                            roughness: 0.2
                        });
                        const bar = new THREE.Mesh(barGeo, barMat);
                        bar.position.set(x, -1.3, -0.85);
                        electricalGroup.add(bar);
                        busbars.push(bar);
                    });

                    // Dynamic Arc Flash / Spark Discharge Particles
                    const sparkCount = 28;
                    const sparkGeo = new THREE.SphereGeometry(0.035, 6, 6);
                    const sparkMat = new THREE.MeshBasicMaterial({ color: 0x88ccff });
                    for (let i = 0; i < sparkCount; i++) {
                        const spark = new THREE.Mesh(sparkGeo, sparkMat.clone());
                        resetElectricalSpark(spark);
                        electricalGroup.add(spark);
                        electricalSparks.push(spark);
                    }

                    sparkLight = new THREE.PointLight(0x66bbff, 3.2, 10);
                    sparkLight.position.set(0, -1.2, -0.6);
                    electricalGroup.add(sparkLight);

                    // INTERACTIVE MASTER ROTARY ISOLATION HANDLE & GROUNDING LEVER
                    const handleBaseGeo = new THREE.CylinderGeometry(0.2, 0.2, 0.1, 16);
                    const handleBaseMat = new THREE.MeshStandardMaterial({ color: 0xd35400, metalness: 0.7 });
                    const handleBase = new THREE.Mesh(handleBaseGeo, handleBaseMat);
                    handleBase.rotation.x = Math.PI / 2;
                    handleBase.position.set(0.65, -0.3, -0.95);
                    electricalGroup.add(handleBase);

                    const leverGeo = new THREE.BoxGeometry(0.08, 0.55, 0.12);
                    const leverMat = new THREE.MeshStandardMaterial({
                        color: 0x27ae60,
                        emissive: 0x1e8449,
                        emissiveIntensity: 1.0
                    });
                    isolatorHandleMesh = new THREE.Mesh(leverGeo, leverMat);
                    isolatorHandleMesh.position.set(0.65, -0.15, -0.9);
                    electricalGroup.add(isolatorHandleMesh);

                    // Glowing Ring targeting isolator switch
                    const isolatorRingGeo = new THREE.RingGeometry(0.35, 0.44, 24);
                    const isolatorRingMat = new THREE.MeshBasicMaterial({ color: 0x00ff88, side: THREE.DoubleSide });
                    const isolatorRing = new THREE.Mesh(isolatorRingGeo, isolatorRingMat);
                    isolatorRing.position.set(0.65, -0.3, -0.88);
                    electricalGroup.add(isolatorRing);
                    electricalGroup.isolatorRing = isolatorRing;
                }

                function resetElectricalSpark(spark) {
                    spark.position.set(
                        (Math.random() - 0.5) * 0.7,
                        -1.3 + (Math.random() - 0.5) * 0.4,
                        -0.8 + (Math.random() - 0.5) * 0.2
                    );
                    spark.userData = {
                        vx: (Math.random() - 0.5) * 0.04,
                        vy: (Math.random() - 0.5) * 0.04,
                        vz: (Math.random() * 0.05),
                        life: 0.8 + Math.random() * 0.6
                    };
                }

                // ================= 5. MANDATORY PPE INSPECTION =================
                function setupPpeInspectionSimulation() {
                    ppeWorkbenchGroup = new THREE.Group();
                    rig.add(ppeWorkbenchGroup);

                    // Vocational Equipment Inspection Workbench
                    const benchGeo = new THREE.BoxGeometry(4.2, 0.18, 2.2);
                    const benchMat = new THREE.MeshStandardMaterial({ color: 0x3d312a, roughness: 0.8, metalness: 0.2 });
                    const bench = new THREE.Mesh(benchGeo, benchMat);
                    bench.position.set(0, -1.8, 1.2);
                    ppeWorkbenchGroup.add(bench);

                    // 4 Workbench Heavy Steel Legs
                    [[-1.8, 0.4], [1.8, 0.4], [-1.8, 2.0], [1.8, 2.0]].forEach(coord => {
                        const legGeo = new THREE.BoxGeometry(0.12, 1.2, 0.12);
                        const leg = new THREE.Mesh(legGeo, benchMat);
                        leg.position.set(coord[0], -2.4, coord[1]);
                        ppeWorkbenchGroup.add(leg);
                    });

                    // 1. DRÄGER CHEMICAL OXYGEN SELF-RESCUER (Canister, Breathing Hose, Indicator)
                    const scsrCanisterGeo = new THREE.CylinderGeometry(0.24, 0.24, 0.75, 16);
                    const scsrMat = new THREE.MeshStandardMaterial({
                        color: 0xb03a2e,
                        emissive: 0x78281f,
                        emissiveIntensity: 0.6,
                        metalness: 0.5
                    });
                    selfRescuerMesh = new THREE.Mesh(scsrCanisterGeo, scsrMat);
                    selfRescuerMesh.position.set(-1.2, -1.35, 1.2);
                    selfRescuerMesh.rotation.z = Math.PI / 8;
                    ppeWorkbenchGroup.add(selfRescuerMesh);

                    // Moisture Indicator Bead (Blue/Green = Intact)
                    const beadGeo = new THREE.SphereGeometry(0.06, 12, 12);
                    const beadMat = new THREE.MeshBasicMaterial({ color: 0x00ffcc });
                    const moistureBead = new THREE.Mesh(beadGeo, beadMat);
                    moistureBead.position.set(-1.1, -1.1, 1.4);
                    ppeWorkbenchGroup.add(moistureBead);

                    // 2. MINER LED CAP LAMP & BATTERY PACK
                    const lampGeo = new THREE.CylinderGeometry(0.18, 0.22, 0.22, 16);
                    const lampMat = new THREE.MeshStandardMaterial({
                        color: 0x111111,
                        emissive: 0xffffff,
                        emissiveIntensity: 2.2
                    });
                    capLampMesh = new THREE.Mesh(lampGeo, lampMat);
                    capLampMesh.rotation.x = Math.PI / 2;
                    capLampMesh.position.set(-0.35, -1.58, 1.4);
                    ppeWorkbenchGroup.add(capLampMesh);

                    const batteryPackGeo = new THREE.BoxGeometry(0.35, 0.45, 0.2);
                    const batteryMat = new THREE.MeshStandardMaterial({ color: 0x27ae60, metalness: 0.6 });
                    const battery = new THREE.Mesh(batteryPackGeo, batteryMat);
                    battery.position.set(-0.35, -1.5, 0.9);
                    ppeWorkbenchGroup.add(battery);

                    // Heavy Duty Cord connecting lamp to battery
                    const cordGeo = new THREE.TorusGeometry(0.28, 0.025, 8, 20, Math.PI);
                    const cordMat = new THREE.MeshStandardMaterial({ color: 0x050505, roughness: 0.9 });
                    const cord = new THREE.Mesh(cordGeo, cordMat);
                    cord.position.set(-0.35, -1.6, 1.15);
                    ppeWorkbenchGroup.add(cord);

                    // 3. MINING HARD HAT WITH REFLECTIVE STRIPS
                    const helmetGeo = new THREE.SphereGeometry(0.38, 16, 16, 0, Math.PI * 2, 0, Math.PI * 0.6);
                    const helmetMat = new THREE.MeshStandardMaterial({
                        color: 0xf39c12, // High-vis Safety Orange
                        roughness: 0.3,
                        metalness: 0.2
                    });
                    helmetMesh = new THREE.Mesh(helmetGeo, helmetMat);
                    helmetMesh.position.set(0.5, -1.5, 1.2);
                    ppeWorkbenchGroup.add(helmetMesh);

                    // Reflective Silver Band around helmet
                    const reflexGeo = new THREE.TorusGeometry(0.38, 0.03, 8, 24);
                    const reflexMat = new THREE.MeshBasicMaterial({ color: 0xffffff });
                    const reflex = new THREE.Mesh(reflexGeo, reflexMat);
                    reflex.rotation.x = Math.PI / 2;
                    reflex.position.set(0.5, -1.5, 1.2);
                    ppeWorkbenchGroup.add(reflex);

                    // 4. DIGITAL MULTI-GAS DETECTOR INSTRUMENT
                    const detectorGeo = new THREE.BoxGeometry(0.3, 0.55, 0.14);
                    const detectorMat = new THREE.MeshStandardMaterial({ color: 0xe67e22, roughness: 0.4 });
                    gasDetectorMesh = new THREE.Mesh(detectorGeo, detectorMat);
                    gasDetectorMesh.position.set(1.35, -1.55, 1.3);
                    gasDetectorMesh.rotation.y = -Math.PI / 6;
                    ppeWorkbenchGroup.add(gasDetectorMesh);

                    // LCD Display on Gas Detector
                    const lcdGeo = new THREE.PlaneGeometry(0.2, 0.18);
                    const lcdMat = new THREE.MeshBasicMaterial({ color: 0x00ff88, side: THREE.DoubleSide });
                    const lcd = new THREE.Mesh(lcdGeo, lcdMat);
                    lcd.position.set(1.3, -1.48, 1.38);
                    lcd.rotation.y = -Math.PI / 6;
                    ppeWorkbenchGroup.add(lcd);

                    // Pulsing Inspection Hotspot Ring
                    const ringGeo = new THREE.RingGeometry(0.7, 0.85, 32);
                    const ringMat = new THREE.MeshBasicMaterial({ color: 0x00ffaa, side: THREE.DoubleSide, transparent: true, opacity: 0.85 });
                    ppeTargetRing = new THREE.Mesh(ringGeo, ringMat);
                    ppeTargetRing.rotation.x = -Math.PI / 2;
                    ppeTargetRing.position.set(0, -1.68, 1.2);
                    ppeWorkbenchGroup.add(ppeTargetRing);

                    const ppeLight = new THREE.PointLight(0xffeedd, 2.0, 8);
                    ppeLight.position.set(0, -0.4, 1.5);
                    ppeWorkbenchGroup.add(ppeLight);
                }

                // ================= TOUCH CONTROLS & INTERACTION =================
                function setupTouchControls() {
                    const dom = renderer.domElement;
                    dom.addEventListener('pointerdown', e => {
                        isDown = true;
                        lastX = e.clientX;
                        lastY = e.clientY;
                    });
                    window.addEventListener('pointerup', () => { isDown = false; });
                    window.addEventListener('pointercancel', () => { isDown = false; });
                    window.addEventListener('pointermove', e => {
                        if (!isDown) return;
                        const dx = e.clientX - lastX;
                        const dy = e.clientY - lastY;
                        rotY += dx * 0.005;
                        rotX += dy * 0.003;
                        rotX = Math.max(-0.35, Math.min(0.35, rotX));
                        lastX = e.clientX;
                        lastY = e.clientY;
                    });

                    dom.addEventListener('click', e => {
                        handleTapInteraction();
                    });
                }

                function handleTapInteraction() {
                    if (SIM_TYPE === 'gas') {
                        if (gasInteractiveMesh) {
                            gasInteractiveMesh.material.emissiveIntensity = 3.2;
                            setTimeout(() => { gasInteractiveMesh.material.emissiveIntensity = 1.2; }, 300);
                        }
                        if (window.AndroidInterface) window.AndroidInterface.onHazardFound('gas_valve_isolated');
                    } else if (SIM_TYPE === 'fire') {
                        if (flameLight) {
                            flameLight.intensity = 5.5;
                            setTimeout(() => { flameLight.intensity = 2.8; }, 300);
                        }
                        if (window.AndroidInterface) window.AndroidInterface.onHazardFound('evacuation_path_confirmed');
                    } else if (SIM_TYPE === 'machinery') {
                        isMachineLockedOut = !isMachineLockedOut;
                        if (lotoStationMesh) {
                            lotoStationMesh.material.emissiveIntensity = 3.0;
                            setTimeout(() => { lotoStationMesh.material.emissiveIntensity = 0.8; }, 300);
                        }
                        if (window.AndroidInterface) window.AndroidInterface.onHazardFound('loto_isolated');
                    } else if (SIM_TYPE === 'electrical') {
                        isCircuitIsolated = !isCircuitIsolated;
                        if (isolatorHandleMesh) {
                            isolatorHandleMesh.rotation.z = isCircuitIsolated ? -Math.PI / 2 : 0;
                            isolatorHandleMesh.material.emissiveIntensity = 3.0;
                            setTimeout(() => { isolatorHandleMesh.material.emissiveIntensity = 1.0; }, 300);
                        }
                        if (window.AndroidInterface) window.AndroidInterface.onHazardFound('electrical_grounded');
                    } else if (SIM_TYPE === 'ppe') {
                        isPpeInspected = true;
                        if (ppeTargetRing) {
                            ppeTargetRing.scale.set(1.4, 1.4, 1.4);
                            setTimeout(() => { ppeTargetRing.scale.set(1.0, 1.0, 1.0); }, 300);
                        }
                        if (window.AndroidInterface) window.AndroidInterface.onHazardFound('ppe_verified');
                    }
                }

                function onWindowResize() {
                    camera.aspect = window.innerWidth / window.innerHeight;
                    camera.updateProjectionMatrix();
                    renderer.setSize(window.innerWidth, window.innerHeight);
                }

                function animate() {
                    requestAnimationFrame(animate);
                    const elapsedTime = clock.getElapsedTime();

                    rig.rotation.y = rotY;
                    rig.rotation.x = rotX;

                    // 1. Gas Fumes Motion
                    if (SIM_TYPE === 'gas' && fumesGroup) {
                        if (fumesGroup.targetRing) {
                            fumesGroup.targetRing.rotation.z += 0.018;
                            const scale = 1.0 + Math.sin(elapsedTime * 3) * 0.12;
                            fumesGroup.targetRing.scale.set(scale, scale, 1);
                        }
                        fumesGroup.particles.forEach(p => {
                            p.position.x += p.userData.vx;
                            p.position.y += p.userData.vy;
                            p.position.z += p.userData.vz;
                            p.scale.addScalar(p.userData.grow * 0.02);
                            p.material.opacity -= 0.004;
                            if (p.position.y < -1.8 || p.material.opacity <= 0.05) {
                                resetFumeParticle(p);
                                p.material.opacity = 0.45;
                            }
                        });
                    }

                    // 2. Fire & Runway Evacuation Motion
                    if (SIM_TYPE === 'fire') {
                        if (fireGroup && fireGroup.flames) {
                            fireGroup.flames.forEach(f => {
                                const wave = Math.sin(elapsedTime * f.userData.speed + f.userData.phase);
                                f.scale.y = 0.8 + wave * 0.35;
                                f.scale.x = 0.85 + Math.cos(elapsedTime * 6) * 0.2;
                                f.position.y = f.userData.baseY + wave * 0.08;
                            });
                            if (flameLight) {
                                flameLight.intensity = 2.4 + Math.sin(elapsedTime * 12) * 0.8 + (Math.random() - 0.5) * 0.4;
                            }
                        }
                        if (arrowsGroup && arrowsGroup.arrows) {
                            const waveProgress = (elapsedTime * 2.8) % arrowsGroup.arrows.length;
                            arrowsGroup.arrows.forEach((arr, idx) => {
                                const dist = Math.abs(idx - waveProgress);
                                const intensity = Math.max(0.6, 2.2 - dist * 0.6);
                                arr.material.emissiveIntensity = intensity;
                                arr.scale.setScalar(1.0 + Math.max(0, (2.2 - dist * 0.6) * 0.15));
                            });
                        }
                        if (alarmStrobeLight) {
                            alarmStrobeLight.intensity = (Math.sin(elapsedTime * 8) > 0.2) ? 3.2 : 0.4;
                        }
                    }

                    // 3. Machinery Motion
                    if (SIM_TYPE === 'machinery' && machineGroup) {
                        if (!isMachineLockedOut) {
                            const rollerSpeed = 0.08;
                            rotatingRollers.forEach(r => { r.rotation.y += rollerSpeed; });
                            if (drivePinchMesh) drivePinchMesh.rotation.y += rollerSpeed * 1.5;
                        }
                        if (machineGroup.lotoRing) {
                            const scale = 1.0 + Math.sin(elapsedTime * 4) * 0.15;
                            machineGroup.lotoRing.scale.set(scale, scale, 1);
                        }
                    }

                    // 4. Electrical Arc Flash Sparks Motion
                    if (SIM_TYPE === 'electrical' && electricalGroup) {
                        if (!isCircuitIsolated) {
                            electricalSparks.forEach(s => {
                                s.position.x += s.userData.vx;
                                s.position.y += s.userData.vy;
                                s.position.z += s.userData.vz;
                                s.userData.life -= 0.03;
                                if (s.userData.life <= 0) resetElectricalSpark(s);
                            });
                            if (sparkLight) {
                                sparkLight.intensity = (Math.random() > 0.3) ? (2.0 + Math.random() * 3.0) : 0.3;
                            }
                        } else {
                            // De-energized safe state
                            electricalSparks.forEach(s => { s.position.set(0, -99, 0); });
                            if (sparkLight) sparkLight.intensity = 0;
                        }
                        if (electricalGroup.isolatorRing) {
                            const scale = 1.0 + Math.sin(elapsedTime * 4) * 0.12;
                            electricalGroup.isolatorRing.scale.set(scale, scale, 1);
                        }
                    }

                    // 5. PPE Inspection Workbench Motion
                    if (SIM_TYPE === 'ppe' && ppeWorkbenchGroup) {
                        if (ppeTargetRing) {
                            ppeTargetRing.rotation.z += 0.012;
                            const scale = 1.0 + Math.sin(elapsedTime * 3) * 0.08;
                            ppeTargetRing.scale.set(scale, scale, 1);
                        }
                        if (selfRescuerMesh) {
                            selfRescuerMesh.position.y = -1.35 + Math.sin(elapsedTime * 1.5) * 0.02;
                        }
                    }

                    renderer.render(scene, camera);
                }

                init();
            </script>
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webChromeClient = WebChromeClient()
                webViewClient = WebViewClient()
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onHazardFound(type: String) {
                        onHazardIdentified(type)
                    }
                }, "AndroidInterface")
                loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        },
        modifier = modifier.fillMaxSize()
    )
}
