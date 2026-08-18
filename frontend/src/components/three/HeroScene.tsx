
import { useRef, useState, useMemo } from 'react';
import { Canvas, useFrame } from '@react-three/fiber';
import { Environment, Float } from '@react-three/drei';
import * as THREE from 'three';

function Box({ position }: { position: [number, number, number] }) {
  const meshRef = useRef<THREE.Mesh>(null);
  const [hovered, setHover] = useState(false);
  
  const targetScale = hovered ? 1.4 : 1;
  const currentScale = useRef(1);

  useFrame((_, delta) => {
    if (meshRef.current) {
      meshRef.current.rotation.x += delta * 0.15;
      meshRef.current.rotation.y += delta * 0.2;
      
      currentScale.current += (targetScale - currentScale.current) * 8 * delta;
      meshRef.current.scale.setScalar(currentScale.current);
    }
  });

  return (
    <Float speed={2} rotationIntensity={1} floatIntensity={1.5} floatingRange={[-0.5, 0.5]}>
      <mesh 
        ref={meshRef} 
        position={position}
        onPointerOver={(e) => { e.stopPropagation(); setHover(true); }}
        onPointerOut={() => setHover(false)}
      >
        <boxGeometry args={[1, 1, 1]} />
        <meshStandardMaterial 
          color={hovered ? '#ffffff' : '#1a1a1a'} 
          roughness={0.2} 
          metalness={0.8}
          emissive={hovered ? '#222222' : '#000000'}
        />
      </mesh>
    </Float>
  );
}

function Boxes() {
  const boxes = useMemo(() => {
    const temp = [];
    for (let i = 0; i < 60; i++) {
      const x = (Math.random() - 0.5) * 25;
      const y = (Math.random() - 0.5) * 15;
      const z = (Math.random() - 0.5) * 10 - 5;
      temp.push({ position: [x, y, z] as [number, number, number] });
    }
    return temp;
  }, []);

  return (
    <>
      {boxes.map((box, i) => (
        <Box key={i} position={box.position} />
      ))}
    </>
  );
}

export function SceneCanvas({ className, interactive = true }: { className?: string; interactive?: boolean }) {
  return (
    <div className={`absolute inset-0 z-0 ${className || ''}`} style={{ pointerEvents: interactive ? 'auto' : 'none' }}>
      <Canvas camera={{ position: [0, 0, 10], fov: 45 }}>
        <ambientLight intensity={0.4} />
        <directionalLight position={[10, 10, 5]} intensity={1.5} />
        <spotLight position={[-10, -10, -10]} intensity={1} color="#ffffff" />
        <Boxes />
        <Environment preset="city" />
      </Canvas>
    </div>
  );
}
