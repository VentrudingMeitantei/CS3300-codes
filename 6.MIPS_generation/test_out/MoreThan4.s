.text
.globl main
main:
move $fp, $sp
sw $ra, -4($fp)
subu $sp, $sp, 48
li $v1, 8
move $s0, $v1
sw $a0, -4($sp)
sw $v0, -8($sp)
move $a0, $s0
li $v0, 9
syscall
move $s0, $v0
lw $a0, -4($sp)
lw $v0, -8($sp)
move $s0, $s0
li $v1, 4
move $s1, $v1
sw $a0, -4($sp)
sw $v0, -8($sp)
move $a0, $s1
li $v0, 9
syscall
move $s1, $v0
lw $a0, -4($sp)
lw $v0, -8($sp)
move $s1, $s1
la $s2, MT4_Change
sw $s2, 4($s0)
la $s2, MT4_Start
sw $s2, 0($s0)
sw $s0, 0($s1)
move $s1, $s1
lw $s0, 0($s1)
lw $s0, 0($s0)
li $v1, 1
move $s2, $v1
li $v1, 2
move $s3, $v1
li $v1, 3
move $s4, $v1
li $v1, 4
move $s5, $v1
li $v1, 5
move $s6, $v1
li $v1, 6
move $s7, $v1
sw $t0, 0($sp)
sw $t1, 4($sp)
sw $t2, 8($sp)
sw $t3, 12($sp)
sw $t4, 16($sp)
sw $t5, 20($sp)
sw $t6, 24($sp)
sw $t7, 28($sp)
sw $t8, 32($sp)
sw $t9, 36($sp)
move $a0, $s1
move $a1, $s2
move $a2, $s3
move $a3, $s4
sw $s5, -12($sp)
sw $s6, -16($sp)
sw $s7, -20($sp)
move $v0, $s0
jalr $v0
lw $t0, 0($sp)
lw $t1, 4($sp)
lw $t2, 8($sp)
lw $t3, 12($sp)
lw $t4, 16($sp)
lw $t5, 20($sp)
lw $t6, 24($sp)
lw $t7, 28($sp)
lw $t8, 32($sp)
lw $t9, 36($sp)
move $s7, $v0
sw $a0, -4($sp)
sw $v0, -8($sp)
move $a0, $s7
li $v0, 1
syscall
la $a0, newl
li $v0, 4
syscall
lw $a0, -4($sp)
lw $v0, -8($sp)
addu $sp, $sp, 48
lw $ra, -4($fp)
li $v0, 10
 syscall
.text
.globl MT4_Start
MT4_Start:
sw $fp, -8($sp)
move $fp, $sp
sw $ra, -4($fp)
subu $sp, $sp, 120
lw $v0, -12($fp)
sw $v0, 0($sp)
lw $v0, -16($fp)
sw $v0, 4($sp)
lw $v0, -20($fp)
sw $v0, 8($sp)
sw $s0, 12($sp)
sw $s1, 16($sp)
sw $s2, 20($sp)
sw $s3, 24($sp)
sw $s4, 28($sp)
sw $s5, 32($sp)
sw $s6, 36($sp)
sw $s7, 40($sp)
move $s0, $a0
move $s1, $a1
move $s2, $a2
move $s3, $a3
sw $a0, -4($sp)
sw $v0, -8($sp)
move $a0, $s1
li $v0, 1
syscall
la $a0, newl
li $v0, 4
syscall
lw $a0, -4($sp)
lw $v0, -8($sp)
sw $a0, -4($sp)
sw $v0, -8($sp)
move $a0, $s2
li $v0, 1
syscall
la $a0, newl
li $v0, 4
syscall
lw $a0, -4($sp)
lw $v0, -8($sp)
sw $a0, -4($sp)
sw $v0, -8($sp)
move $a0, $s3
li $v0, 1
syscall
la $a0, newl
li $v0, 4
syscall
lw $a0, -4($sp)
lw $v0, -8($sp)
lw $v1, 0($sp)
sw $a0, -4($sp)
sw $v0, -8($sp)
move $a0, $v1
li $v0, 1
syscall
la $a0, newl
li $v0, 4
syscall
lw $a0, -4($sp)
lw $v0, -8($sp)
lw $v1, 4($sp)
sw $a0, -4($sp)
sw $v0, -8($sp)
move $a0, $v1
li $v0, 1
syscall
la $a0, newl
li $v0, 4
syscall
lw $a0, -4($sp)
lw $v0, -8($sp)
lw $v1, 8($sp)
sw $a0, -4($sp)
sw $v0, -8($sp)
move $a0, $v1
li $v0, 1
syscall
la $a0, newl
li $v0, 4
syscall
lw $a0, -4($sp)
lw $v0, -8($sp)
move $s0, $s0
lw $s4, 0($s0)
lw $s4, 4($s4)
sw $t0, 44($sp)
sw $t1, 48($sp)
sw $t2, 52($sp)
sw $t3, 56($sp)
sw $t4, 60($sp)
sw $t5, 64($sp)
sw $t6, 68($sp)
sw $t7, 72($sp)
sw $t8, 76($sp)
sw $t9, 80($sp)
move $a0, $s0
lw $v0, 8($sp)
move $a1, $v0
lw $v0, 4($sp)
move $a2, $v0
lw $v0, 0($sp)
move $a3, $v0
sw $s3, -12($sp)
sw $s2, -16($sp)
sw $s1, -20($sp)
move $v0, $s4
jalr $v0
lw $t0, 44($sp)
lw $t1, 48($sp)
lw $t2, 52($sp)
lw $t3, 56($sp)
lw $t4, 60($sp)
lw $t5, 64($sp)
lw $t6, 68($sp)
lw $t7, 72($sp)
lw $t8, 76($sp)
lw $t9, 80($sp)
move $s4, $v0
move $s4, $s4
move $v0, $s4
lw $s0, 12($sp)
lw $s1, 16($sp)
lw $s2, 20($sp)
lw $s3, 24($sp)
lw $s4, 28($sp)
lw $s5, 32($sp)
lw $s6, 36($sp)
lw $s7, 40($sp)
addu $sp, $sp, 120
lw $ra, -4($sp)
lw $fp, -8($sp)
jr $ra
.text
.globl MT4_Change
MT4_Change:
sw $fp, -8($sp)
move $fp, $sp
sw $ra, -4($fp)
subu $sp, $sp, 80
lw $v0, -12($fp)
sw $v0, 0($sp)
lw $v0, -16($fp)
sw $v0, 4($sp)
lw $v0, -20($fp)
sw $v0, 8($sp)
sw $s0, 12($sp)
sw $s1, 16($sp)
sw $s2, 20($sp)
sw $s3, 24($sp)
sw $s4, 28($sp)
sw $s5, 32($sp)
sw $s6, 36($sp)
sw $s7, 40($sp)
move $v1, $a0
move $s0, $a1
move $s1, $a2
move $s2, $a3
sw $a0, -4($sp)
sw $v0, -8($sp)
move $a0, $s0
li $v0, 1
syscall
la $a0, newl
li $v0, 4
syscall
lw $a0, -4($sp)
lw $v0, -8($sp)
sw $a0, -4($sp)
sw $v0, -8($sp)
move $a0, $s1
li $v0, 1
syscall
la $a0, newl
li $v0, 4
syscall
lw $a0, -4($sp)
lw $v0, -8($sp)
sw $a0, -4($sp)
sw $v0, -8($sp)
move $a0, $s2
li $v0, 1
syscall
la $a0, newl
li $v0, 4
syscall
lw $a0, -4($sp)
lw $v0, -8($sp)
lw $v1, 0($sp)
sw $a0, -4($sp)
sw $v0, -8($sp)
move $a0, $v1
li $v0, 1
syscall
la $a0, newl
li $v0, 4
syscall
lw $a0, -4($sp)
lw $v0, -8($sp)
lw $v1, 4($sp)
sw $a0, -4($sp)
sw $v0, -8($sp)
move $a0, $v1
li $v0, 1
syscall
la $a0, newl
li $v0, 4
syscall
lw $a0, -4($sp)
lw $v0, -8($sp)
lw $v1, 8($sp)
sw $a0, -4($sp)
sw $v0, -8($sp)
move $a0, $v1
li $v0, 1
syscall
la $a0, newl
li $v0, 4
syscall
lw $a0, -4($sp)
lw $v0, -8($sp)
li $v1, 0
move $s2, $v1
move $v0, $s2
lw $s0, 12($sp)
lw $s1, 16($sp)
lw $s2, 20($sp)
lw $s3, 24($sp)
lw $s4, 28($sp)
lw $s5, 32($sp)
lw $s6, 36($sp)
lw $s7, 40($sp)
addu $sp, $sp, 80
lw $ra, -4($sp)
lw $fp, -8($sp)
jr $ra

.data
.align 0
newl: .asciiz "\n"
.data
.align 0
str_er: .asciiz "ERROR: abnormal termination\n"
