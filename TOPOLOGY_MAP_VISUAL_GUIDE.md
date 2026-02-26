# 🗺️ Topology Map - Interactive Features Guide

## Layout Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                         TOP TOOLBAR                                  │
│  [← BACK]           Zoom: 1.5×              [🔄 RESET]              │
└─────────────────────────────────────────────────────────────────────┘
│                                                                       │
│                                              ┌──────────────────────┐ │
│                                              │   CLUSTER INFO       │ │
│                                              ├──────────────────────┤ │
│     Grid Background                          │ CLUSTER_NAME         │ │
│     ═══════════════════                      │ Files: 42            │ │
│                                              │ Path: /data/...      │ │
│       ●─────────────────────●               │ Modified: Jan 15     │ │
│      / \    Edges with      / \              │ Duplicates: No       │ │
│     /   \  3-level opacity  /   \            ├──────────────────────┤ │
│    ●     \ (hover/select)   ●     ●         │ [View Details]       │ │
│    │\     \───────────────/  \   /│         ├──────────────────────┤ │
│    │ ●     ●                  ● / │         │ Related Clusters     │ │
│    │   \         ●         /   ●  │         ├──────────────────────┤ │
│    ●     \───────────────/       ● │         │ ✓ Cluster A (10)    │ │
│      \  ● │       │       ●      /  │         │ ◎ Cluster B (25)    │ │
│       \.  \│       │      /       /   │         │ ✓ Cluster C (8)     │ │
│         \  ●       ●      ●    /     │         │ ◎ Cluster D (15)    │ │
│          \          \ ─ ─ ─ ─/       │         └──────────────────────┘ │
│           \──────●──────────/        │                                   │
│                                      │                                   │
│  [CONTROLS]                          │                                   │
│  Drag → Pan view                     │                                   │
│  Scroll → Zoom                       │                                   │
│  Click Node → Select                 │                                   │
│  Drag Node → Move                    │                                   │
│                                      │                                   │
└──────────────────────────────────────┴──────────────────────────────────┘
```

---

## Interactive Elements

### **1. Canvas Area (Left Side)**
- Main visualization with draggable pan and scrollable zoom
- Grid background with 50px spacing for reference
- Dynamic nodes and edges that respond to interaction
- Legend in bottom-left corner

### **2. Cluster Nodes**
```
Default State:           Hovered State:           Selected State:
   ┌─────────┐            ┌─────────┐              ┌─────────┐
   │ CLUSTER │            │ CLUSTER │              │ CLUSTER │
   │   (12)  │   ──→      │   (12)  │    ──→      │   (12)  │
   └─────────┘            └─────────┘              └─────────┘
   SlateBlue            Bright Border          SunsetCoral
   2px border            Thicker Border          3px border
   Opacity: 0.5          Opacity: 0.8            Opacity: 1.0
```

### **3. Connections (Edges)**
```
Default:        Hover Related:      Select Related:
  ○────○          ○─ ─ ─ ─○          ○═══════○
  │    │          │       │          │       │
  ○────○          ○────────○          ○═══════○
 Subtle          Medium             Bright
 (0.2 alpha)     (0.5 alpha)        (0.8 alpha)
 2px width       2px width          3px width
```

### **4. Detail Panel (Right Side)**
- Opens when cluster is selected
- Slides in from the right with animation
- Shows detailed cluster information
- Allows visibility toggling of clusters
- Provides quick navigation to related clusters

---

## Interaction Flow

### **Discovery Flow**
```
1. User sees topology map with all clusters
   ↓
2. User hovers over interesting cluster
   ├─ Edges connected to that cluster brighten
   ├─ Node shows hover state
   └─ Visual feedback without commitment
   ↓
3. User clicks cluster to select it
   ├─ Node turns bright coral
   ├─ Connected edges brighten
   └─ Detail panel opens on right
   ↓
4. User can:
   ├─ Read cluster details in panel
   ├─ View related clusters list
   ├─ Toggle visibility of other clusters
   ├─ Click to switch to different cluster
   └─ Click "View Details" to navigate
```

### **Exploration Flow**
```
1. User drags canvas to pan view
2. User scrolls to zoom in/out
   └─ Zoom level displayed in toolbar
3. User can click "Reset" to return to default
   └─ Scale back to 1.0×
   └─ Pan offset reset to origin
4. User repositions clusters by dragging them
   └─ Positions persist during session
```

---

## State Transitions

### **Node States**
```
                   ┌─────────────┐
                   │   DEFAULT   │
                   │ (SlateBlue) │
                   └─────────────┘
                    ↓   hover   ↑
                    ↓           │
              ┌─────────────┐   │
              │   HOVERED   │───┘
              │(Bright Edge)│
              └─────────────┘
                    ↓   click
                    ↓
              ┌─────────────────┐
              │   SELECTED      │
              │ (SunsetCoral)   │
              │ Panel Opens     │
              └─────────────────┘
```

### **Panel States**
```
┌──────────────────────────────────────────┐
│         No Cluster Selected              │
│              (Hidden)                    │
└──────────────────────────────────────────┘
                  ↓ click node
                  ↓
┌──────────────────────────────────────────┐
│    Cluster Selected (Visible)            │
│  ┌──────────────────────────────────────┐│
│  │       CLUSTER INFO  [X]               ││
│  ├──────────────────────────────────────┤│
│  │ Cluster Details                      ││
│  ├──────────────────────────────────────┤│
│  │ [View Details Button]                ││
│  ├──────────────────────────────────────┤│
│  │ Related Clusters:                    ││
│  │ • Cluster A [👁️]                    ││
│  │ • Cluster B [👁️/off]                ││
│  └──────────────────────────────────────┘│
└──────────────────────────────────────────┘
                  ↑ click X
                  │
         (Returns to hidden)
```

---

## Control Mappings

| Input | Action | Result |
|-------|--------|--------|
| **Mouse Drag** | Pan | Move view while scale stays same |
| **Mouse Scroll** | Zoom | In/out bounded (0.5× to 3.0×) |
| **Click Node** | Select | Open detail panel, highlight cluster |
| **Hover Node** | Preview | Show edges, node feedback |
| **Drag Node** | Reposition | Move node in visualization |
| **Click "Reset"** | Reset View | Return to 1.0× zoom, origin position |
| **Click "Back"** | Navigate | Return to dashboard |
| **Click "View Details"** | Navigate | Open cluster detail view |
| **Click Eye Icon** | Toggle | Show/hide cluster from map |
| **Click Cluster Name** | Switch | Select different cluster |
| **Click "X"** | Close Panel | Hide detail panel |

---

## Color Coding

### **Nodes**
- 🔵 **SlateBlue** (Default): Normal, unselected cluster
- 🔶 **SunsetCoral** (Selected): Currently selected, highlighted
- ⚪ **Transitions**: Smooth gradients between states

### **Edges**
- 🟡 **Faded** (Default): Subtle visibility (0.2 alpha)
- 🟠 **Medium** (Hover): Medium visibility (0.5 alpha)  
- 🔴 **Bright** (Select): High visibility (0.8 alpha)

### **UI Elements**
- 🔵 **SlateBlue**: Buttons, backgrounds
- 🔶 **SunsetCoral**: Highlights, important text
- ⚪ **White**: Primary text
- 🟤 **SoftSand**: Secondary text

---

## Keyboard & Mouse Shortcuts Reference

```
┌─────────────────────────────────────────────────┐
│        TOPOLOGY MAP CONTROLS LEGEND             │
├─────────────────────────────────────────────────┤
│ Action          │ Input                         │
├─────────────────────────────────────────────────┤
│ Pan View        │ Click + Drag on canvas        │
│ Zoom In         │ Scroll Up / Mouse Wheel ↑     │
│ Zoom Out        │ Scroll Down / Mouse Wheel ↓   │
│ Select Cluster  │ Click on cluster node         │
│ Move Cluster    │ Click + Drag cluster node     │
│ Show Details    │ Click on cluster name in list │
│ Toggle Visibility│ Click eye icon               │
│ Reset View      │ Click [🔄 RESET] button      │
│ Close Panel     │ Click [X] button              │
│ Navigate Back   │ Click [← BACK] button         │
│ View Full Info  │ Click [View Details] button  │
└─────────────────────────────────────────────────┘
```

---

## Visual Feedback Summary

- ✅ **Hover Feedback**: Border enhancement, edge brightening
- ✅ **Selection Feedback**: Color change, panel open, edge highlight
- ✅ **Interaction Feedback**: Smooth transitions, visual states
- ✅ **Navigation Feedback**: Toolbar zoom display
- ✅ **Spatial Feedback**: Grid background for reference
- ✅ **Legend Feedback**: Always visible controls guide

---

