<template>
  <div class="characters-root">
    <div ref="purpleRef" class="character purple" :style="purpleStyle">
      <div class="eyes" :style="purpleEyesStyle">
        <EyeBall :size="18" :pupil-size="7" :max-distance="5" :is-blinking="isPurpleBlinking" :force-look-x="purpleForceLook.x" :force-look-y="purpleForceLook.y" />
        <EyeBall :size="18" :pupil-size="7" :max-distance="5" :is-blinking="isPurpleBlinking" :force-look-x="purpleForceLook.x" :force-look-y="purpleForceLook.y" />
      </div>
    </div>

    <div ref="blackRef" class="character black" :style="blackStyle">
      <div class="eyes" :style="blackEyesStyle">
        <EyeBall :size="16" :pupil-size="6" :max-distance="4" :is-blinking="isBlackBlinking" :force-look-x="blackForceLook.x" :force-look-y="blackForceLook.y" />
        <EyeBall :size="16" :pupil-size="6" :max-distance="4" :is-blinking="isBlackBlinking" :force-look-x="blackForceLook.x" :force-look-y="blackForceLook.y" />
      </div>
    </div>

    <div ref="orangeRef" class="character orange" :style="orangeStyle">
      <div class="eyes minimalist" :style="orangeEyesStyle">
        <Pupil :size="12" :max-distance="5" :force-look-x="sideForceLook.x" :force-look-y="sideForceLook.y" />
        <Pupil :size="12" :max-distance="5" :force-look-x="sideForceLook.x" :force-look-y="sideForceLook.y" />
      </div>
    </div>

    <div ref="yellowRef" class="character yellow" :style="yellowStyle">
      <div class="eyes minimalist" :style="yellowEyesStyle">
        <Pupil :size="12" :max-distance="5" :force-look-x="sideForceLook.x" :force-look-y="sideForceLook.y" />
        <Pupil :size="12" :max-distance="5" :force-look-x="sideForceLook.x" :force-look-y="sideForceLook.y" />
      </div>
      <div class="mouth" :style="yellowMouthStyle"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, onBeforeUnmount, onMounted, ref, watch } from 'vue'

interface Props {
  isTyping?: boolean
  showPassword?: boolean
  passwordLength?: number
}

const props = withDefaults(defineProps<Props>(), {
  isTyping: false,
  showPassword: false,
  passwordLength: 0
})

interface LookPoint { x?: number; y?: number }

const mouseX = ref(0)
const mouseY = ref(0)
const isPurpleBlinking = ref(false)
const isBlackBlinking = ref(false)
const isLookingAtEachOther = ref(false)
const isPurplePeeking = ref(false)
const purpleRef = ref<HTMLDivElement>()
const blackRef = ref<HTMLDivElement>()
const yellowRef = ref<HTMLDivElement>()
const orangeRef = ref<HTMLDivElement>()
let purpleBlinkTimer: number | undefined
let purpleBlinkInnerTimer: number | undefined
let blackBlinkTimer: number | undefined
let blackBlinkInnerTimer: number | undefined
let typingTimer: number | undefined
let peekTimer: number | undefined
let peekInnerTimer: number | undefined

const updateMouse = (event: MouseEvent) => { mouseX.value = event.clientX; mouseY.value = event.clientY }

function schedulePurpleBlink() {
  purpleBlinkTimer = window.setTimeout(() => {
    isPurpleBlinking.value = true
    purpleBlinkInnerTimer = window.setTimeout(() => {
      isPurpleBlinking.value = false
      schedulePurpleBlink()
    }, 150)
  }, Math.random() * 4000 + 3000)
}

function scheduleBlackBlink() {
  blackBlinkTimer = window.setTimeout(() => {
    isBlackBlinking.value = true
    blackBlinkInnerTimer = window.setTimeout(() => {
      isBlackBlinking.value = false
      scheduleBlackBlink()
    }, 150)
  }, Math.random() * 4000 + 3000)
}

function clearPeekTimers() {
  if (peekTimer) window.clearTimeout(peekTimer)
  if (peekInnerTimer) window.clearTimeout(peekInnerTimer)
}

function schedulePeek() {
  clearPeekTimers()
  if (!(props.passwordLength > 0 && props.showPassword)) {
    isPurplePeeking.value = false
    return
  }
  peekTimer = window.setTimeout(() => {
    isPurplePeeking.value = true
    peekInnerTimer = window.setTimeout(() => {
      isPurplePeeking.value = false
      schedulePeek()
    }, 800)
  }, Math.random() * 3000 + 2000)
}

watch(() => props.isTyping, (value) => {
  if (typingTimer) window.clearTimeout(typingTimer)
  if (value) {
    isLookingAtEachOther.value = true
    typingTimer = window.setTimeout(() => { isLookingAtEachOther.value = false }, 800)
  } else {
    isLookingAtEachOther.value = false
  }
})

watch(() => [props.passwordLength, props.showPassword], () => schedulePeek(), { immediate: true })

function calculatePosition(target: typeof purpleRef) {
  if (!target.value) return { faceX: 0, faceY: 0, bodySkew: 0 }
  const rect = target.value.getBoundingClientRect()
  const centerX = rect.left + rect.width / 2
  const centerY = rect.top + rect.height / 3
  const deltaX = mouseX.value - centerX
  const deltaY = mouseY.value - centerY
  return {
    faceX: Math.max(-15, Math.min(15, deltaX / 20)),
    faceY: Math.max(-10, Math.min(10, deltaY / 30)),
    bodySkew: Math.max(-6, Math.min(6, -deltaX / 120))
  }
}

const purplePos = computed(() => calculatePosition(purpleRef))
const blackPos = computed(() => calculatePosition(blackRef))
const yellowPos = computed(() => calculatePosition(yellowRef))
const orangePos = computed(() => calculatePosition(orangeRef))
const isHidingPassword = computed(() => props.passwordLength > 0 && !props.showPassword)
const purpleForceLook = computed<LookPoint>(() => props.passwordLength > 0 && props.showPassword ? { x: isPurplePeeking.value ? 4 : -4, y: isPurplePeeking.value ? 5 : -4 } : isLookingAtEachOther.value ? { x: 3, y: 4 } : {})
const blackForceLook = computed<LookPoint>(() => props.passwordLength > 0 && props.showPassword ? { x: -4, y: -4 } : isLookingAtEachOther.value ? { x: 0, y: -4 } : {})
const sideForceLook = computed<LookPoint>(() => props.passwordLength > 0 && props.showPassword ? { x: -5, y: -4 } : {})
const purpleStyle = computed(() => ({ transform: props.passwordLength > 0 && props.showPassword ? 'skewX(0deg)' : props.isTyping || isHidingPassword.value ? `skewX(${purplePos.value.bodySkew - 12}deg) translateX(40px)` : `skewX(${purplePos.value.bodySkew}deg)`, height: props.isTyping || isHidingPassword.value ? '440px' : '400px' }))
const blackStyle = computed(() => ({ transform: props.passwordLength > 0 && props.showPassword ? 'skewX(0deg)' : isLookingAtEachOther.value ? `skewX(${blackPos.value.bodySkew * 1.5 + 10}deg) translateX(20px)` : props.isTyping || isHidingPassword.value ? `skewX(${blackPos.value.bodySkew * 1.5}deg)` : `skewX(${blackPos.value.bodySkew}deg)` }))
const orangeStyle = computed(() => ({ transform: props.passwordLength > 0 && props.showPassword ? 'skewX(0deg)' : `skewX(${orangePos.value.bodySkew}deg)` }))
const yellowStyle = computed(() => ({ transform: props.passwordLength > 0 && props.showPassword ? 'skewX(0deg)' : `skewX(${yellowPos.value.bodySkew}deg)` }))
const purpleEyesStyle = computed(() => ({ left: props.passwordLength > 0 && props.showPassword ? '20px' : isLookingAtEachOther.value ? '55px' : `${45 + purplePos.value.faceX}px`, top: props.passwordLength > 0 && props.showPassword ? '35px' : isLookingAtEachOther.value ? '65px' : `${40 + purplePos.value.faceY}px` }))
const blackEyesStyle = computed(() => ({ left: props.passwordLength > 0 && props.showPassword ? '10px' : isLookingAtEachOther.value ? '32px' : `${26 + blackPos.value.faceX}px`, top: props.passwordLength > 0 && props.showPassword ? '28px' : isLookingAtEachOther.value ? '12px' : `${32 + blackPos.value.faceY}px` }))
const orangeEyesStyle = computed(() => ({ left: props.passwordLength > 0 && props.showPassword ? '50px' : `${82 + orangePos.value.faceX}px`, top: props.passwordLength > 0 && props.showPassword ? '85px' : `${90 + orangePos.value.faceY}px` }))
const yellowEyesStyle = computed(() => ({ left: props.passwordLength > 0 && props.showPassword ? '20px' : `${52 + yellowPos.value.faceX}px`, top: props.passwordLength > 0 && props.showPassword ? '35px' : `${40 + yellowPos.value.faceY}px` }))
const yellowMouthStyle = computed(() => ({ left: props.passwordLength > 0 && props.showPassword ? '10px' : `${40 + yellowPos.value.faceX}px`, top: props.passwordLength > 0 && props.showPassword ? '88px' : `${88 + yellowPos.value.faceY}px` }))

onMounted(() => {
  window.addEventListener('mousemove', updateMouse)
  schedulePurpleBlink()
  scheduleBlackBlink()
})

onBeforeUnmount(() => {
  window.removeEventListener('mousemove', updateMouse)
  if (purpleBlinkTimer) window.clearTimeout(purpleBlinkTimer)
  if (purpleBlinkInnerTimer) window.clearTimeout(purpleBlinkInnerTimer)
  if (blackBlinkTimer) window.clearTimeout(blackBlinkTimer)
  if (blackBlinkInnerTimer) window.clearTimeout(blackBlinkInnerTimer)
  if (typingTimer) window.clearTimeout(typingTimer)
  clearPeekTimers()
})

const Pupil = defineComponent({
  name: 'Pupil',
  props: { size: { type: Number, default: 12 }, maxDistance: { type: Number, default: 5 }, forceLookX: { type: Number, default: undefined }, forceLookY: { type: Number, default: undefined } },
  setup(pupilProps) {
    const pupilRef = ref<HTMLDivElement>()
    const localMouseX = ref(0)
    const localMouseY = ref(0)
    const onMove = (event: MouseEvent) => { localMouseX.value = event.clientX; localMouseY.value = event.clientY }
    onMounted(() => window.addEventListener('mousemove', onMove))
    onBeforeUnmount(() => window.removeEventListener('mousemove', onMove))
    const styleValue = computed(() => {
      let x = 0
      let y = 0
      if (pupilProps.forceLookX !== undefined && pupilProps.forceLookY !== undefined) {
        x = pupilProps.forceLookX
        y = pupilProps.forceLookY
      } else if (pupilRef.value) {
        const rect = pupilRef.value.getBoundingClientRect()
        const centerX = rect.left + rect.width / 2
        const centerY = rect.top + rect.height / 2
        const deltaX = localMouseX.value - centerX
        const deltaY = localMouseY.value - centerY
        const distance = Math.min(Math.sqrt(deltaX ** 2 + deltaY ** 2), pupilProps.maxDistance)
        const angle = Math.atan2(deltaY, deltaX)
        x = Math.cos(angle) * distance
        y = Math.sin(angle) * distance
      }
      return { width: `${pupilProps.size}px`, height: `${pupilProps.size}px`, transform: `translate(${x}px, ${y}px)` }
    })
    return () => h('div', { ref: pupilRef, class: 'pupil-node', style: styleValue.value })
  }
})

const EyeBall = defineComponent({
  name: 'EyeBall',
  props: { size: { type: Number, default: 48 }, pupilSize: { type: Number, default: 16 }, maxDistance: { type: Number, default: 10 }, isBlinking: { type: Boolean, default: false }, forceLookX: { type: Number, default: undefined }, forceLookY: { type: Number, default: undefined } },
  setup(eyeProps) {
    const eyeRef = ref<HTMLDivElement>()
    const localMouseX = ref(0)
    const localMouseY = ref(0)
    const onMove = (event: MouseEvent) => { localMouseX.value = event.clientX; localMouseY.value = event.clientY }
    onMounted(() => window.addEventListener('mousemove', onMove))
    onBeforeUnmount(() => window.removeEventListener('mousemove', onMove))
    const pupilStyle = computed(() => {
      let x = 0
      let y = 0
      if (eyeProps.forceLookX !== undefined && eyeProps.forceLookY !== undefined) {
        x = eyeProps.forceLookX
        y = eyeProps.forceLookY
      } else if (eyeRef.value) {
        const rect = eyeRef.value.getBoundingClientRect()
        const centerX = rect.left + rect.width / 2
        const centerY = rect.top + rect.height / 2
        const deltaX = localMouseX.value - centerX
        const deltaY = localMouseY.value - centerY
        const distance = Math.min(Math.sqrt(deltaX ** 2 + deltaY ** 2), eyeProps.maxDistance)
        const angle = Math.atan2(deltaY, deltaX)
        x = Math.cos(angle) * distance
        y = Math.sin(angle) * distance
      }
      return { width: `${eyeProps.pupilSize}px`, height: `${eyeProps.pupilSize}px`, transform: `translate(${x}px, ${y}px)` }
    })
    const eyeStyle = computed(() => ({ width: `${eyeProps.size}px`, height: eyeProps.isBlinking ? '2px' : `${eyeProps.size}px` }))
    return () => h('div', { ref: eyeRef, class: 'eyeball-node', style: eyeStyle.value }, eyeProps.isBlinking ? [] : [h('div', { class: 'eyeball-pupil', style: pupilStyle.value })])
  }
})
</script>

<style scoped>
.characters-root { position: relative; width: 550px; height: 400px; }
.character { position: absolute; bottom: 0; transition: all .7s ease-in-out; transform-origin: bottom center; }
.purple { left: 70px; width: 180px; height: 400px; background: #6c3ff5; border-radius: 10px 10px 0 0; z-index: 1; }
.black { left: 240px; width: 120px; height: 310px; background: #2d2d2d; border-radius: 8px 8px 0 0; z-index: 2; }
.orange { left: 0; width: 240px; height: 200px; background: #ff9b6b; border-radius: 120px 120px 0 0; z-index: 3; }
.yellow { left: 310px; width: 140px; height: 230px; background: #e8d754; border-radius: 70px 70px 0 0; z-index: 4; }
.eyes { position: absolute; display: flex; gap: 8px; transition: all .2s ease-out; }
.minimalist { gap: 24px; }
.mouth { position: absolute; width: 80px; height: 4px; background: #2d2d2d; border-radius: 999px; transition: all .2s ease-out; }
:deep(.eyeball-node) { display: flex; align-items: center; justify-content: center; border-radius: 999px; overflow: hidden; background: #fff; transition: all .15s ease; }
:deep(.eyeball-pupil), :deep(.pupil-node) { border-radius: 50%; background: #2d2d2d; transition: transform .1s ease-out; }
@media (max-width: 640px) { .characters-root { width: 420px; height: 300px; transform: scale(.78); transform-origin: center; } }
</style>