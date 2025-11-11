package com.example.echovision.utils

val labels = arrayOf(
    "person",
    "bicycle",
    "car",
    "motorcycle",
    "airplane",
    "bus",
    "train",
    "truck",
    "boat",
    "traffic light",
    "fire hydrant",
    "",
    "stop sign",
    "parking meter",
    "bench",
    "bird",
    "cat",
    "dog",
    "horse",
    "sheep",
    "cow",
    "elephant",
    "bear",
    "zebra",
    "giraffe",
    "",
    "backpack",
    "umbrella",
    "",
    "",
    "handbag",
    "tie",
    "suitcase",
    "frisbee",
    "skis",
    "snowboard",
    "sports ball",
    "kite",
    "baseball bat",
    "baseball glove",
    "skateboard",
    "surfboard",
    "tennis racket",
    "bottle",
    "",
    "wine glass",
    "cup",
    "fork",
    "knife",
    "spoon",
    "bowl",
    "banana",
    "apple",
    "sandwich",
    "orange",
    "broccoli",
    "carrot",
    "hot dog",
    "pizza",
    "donut",
    "cake",
    "chair",
    "couch",
    "potted plant",
    "bed",
    "",
    "dining table",
    "",
    "",
    "toilet",
    "",
    "tv",
    "laptop",
    "mouse",
    "remote",
    "keyboard",
    "cell phone",
    "microwave",
    "oven",
    "toaster",
    "sink",
    "refrigerator",
    "",
    "book",
    "clock",
    "vase",
    "scissors",
    "teddy bear",
    "hair drier",
    "toothbrush"
)

val dangerSortedIndices = arrayOf(
    2,  // car
    5,  // bus
    7,  // truck
    6,  // train
    3,  // motorcycle
    1,  // bicycle
    8,  // boat
    0,  // person
    48, // knife
    85, // scissors
    24, // giraffe
    23, // zebra
    22, // bear
    21, // elephant
    19, // sheep
    20, // cow
    18, // horse
    17, // dog
    16, // cat
    15, // bird
    12, // stop sign
    9,  // traffic light
    13, // parking meter
    14, // bench,
    61, // chair
    62, // couch
    65, // dining table
    69, // toilet
    79, // sink
    80, // refrigerator
    26, // backpack
    27, // umbrella
    30, // handbag
    31, // tie
    32, // suitcase
    33, // frisbee
    37, // kite
    35, // snowboard
    34, // skis
    36, // sports ball
    40, // surfboard
    39, // skateboard
    41, // tennis racket
    42, // bottle
    46, // cup
    50, // bowl
    45, // wine glass
    47, // fork
    49, // spoon
    51, // banana
    52, // apple
    53, // sandwich
    54, // orange
    55, // broccoli
    56, // carrot
    57, // hot dog
    58, // pizza
    59, // donut
    60, // cake
    63, // potted plant
    70, // tv
    71, // laptop
    72, // mouse
    73, // remote
    74, // keyboard
    75, // cell phone
    76, // microwave
    77, // oven
    78, // toaster
    82, // book
    83, // clock
    84, // vase
    86, // teddy bear
    87, // hair drier
    88, // toothbrush
    // Blank or missing labels at end:
    11, 25, 28, 29, 43, 64, 66, 67, 68, 81
)

val dangerLevel = arrayOf(
    7,   // 0: person
    5,   // 1: bicycle
    0,   // 2: car
    4,   // 3: motorcycle
    -1,  // 4: airplane (not in danger list)
    1,   // 5: bus
    3,   // 6: train
    2,   // 7: truck
    6,   // 8: boat
    21,  // 9: traffic light
    -1,  // 10: fire hydrant (not in list)
    89,  // 11: blank
    20,  // 12: stop sign
    22,  // 13: parking meter
    23,  // 14: bench
    19,  // 15: bird
    18,  // 16: cat
    17,  // 17: dog
    16,  // 18: horse
    14,  // 19: sheep
    15,  // 20: cow
    13,  // 21: elephant
    12,  // 22: bear
    11,  // 23: zebra
    10,  // 24: giraffe
    90,  // 25: blank
    30,  // 26: backpack
    31,  // 27: umbrella
    91,  // 28: blank
    92,  // 29: blank
    32,  // 30: handbag
    33,  // 31: tie
    34,  // 32: suitcase
    35,  // 33: frisbee
    38,  // 34: skis
    37,  // 35: snowboard
    39,  // 36: sports ball
    36,  // 37: kite
    -1,  // 38: baseball bat (not in list)
    41,  // 39: skateboard
    40,  // 40: surfboard
    42,  // 41: tennis racket
    43,  // 42: bottle
    93,  // 43: blank
    -1,  // 44: (missing, between bottle and wine glass)
    46,  // 45: wine glass
    44,  // 46: cup
    47,  // 47: fork
    8,   // 48: knife
    48,  // 49: spoon
    45,  // 50: bowl
    49,  // 51: banana
    50,  // 52: apple
    51,  // 53: sandwich
    52,  // 54: orange
    53,  // 55: broccoli
    54,  // 56: carrot
    55,  // 57: hot dog
    56,  // 58: pizza
    57,  // 59: donut
    58,  // 60: cake
    24,  // 61: chair
    25,  // 62: couch
    59,  // 63: potted plant
    94,  // 64: blank
    26,  // 65: dining table
    95,  // 66: blank
    96,  // 67: blank
    97,  // 68: blank
    27,  // 69: toilet
    60,  // 70: tv
    61,  // 71: laptop
    62,  // 72: mouse
    63,  // 73: remote
    64,  // 74: keyboard
    65,  // 75: cell phone
    66,  // 76: microwave
    67,  // 77: oven
    68,  // 78: toaster
    28,  // 79: sink
    29,  // 80: refrigerator
    98,  // 81: blank
    69,  // 82: book
    70,  // 83: clock
    71,  // 84: vase
    9,   // 85: scissors
    72,  // 86: teddy bear
    73,  // 87: hair drier
    74   // 88: toothbrush
)