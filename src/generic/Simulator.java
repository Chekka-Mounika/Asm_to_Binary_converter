package generic;

import java.io.FileInputStream;

import generic.Operand.OperandType;
import java.io.FileOutputStream;
import java.io.*;
import java.util.*;
import java.nio.*;
import java.io.BufferedOutputStream;

public class Simulator 
{
		
	static FileInputStream inputcodeStream = null;
	
	public static void setupSimulation(String assemblyProgramFile)
	{	
		int firstCodeAddress = ParsedProgram.parseDataSection(assemblyProgramFile);
		ParsedProgram.parseCodeSection(assemblyProgramFile, firstCodeAddress);
		ParsedProgram.printState();
	}

	private static String extra_zeroes(String ptr,int n)
	{
		for(int i=0;i<n;i++)
		{
			ptr+="0";
		}
		return ptr;
	}

	private static String str_converter(int src_opeval, int qtr) 
	{
		if (src_opeval>=0) 
		{
			int var1=0;
			String str=Integer.toBinaryString(src_opeval);
			int n=str.length();
			if(n<qtr)
			{
				var1=qtr-n;
				for(int i=0;i<var1;i++)
				{
					str="0"+str;
				}
				
			}
			return str;
			/*while(n<precision)
			{
				str="0"+str;
				n++;
			}
			return str;*/
		}
		else 
		{
			String str=Integer.toBinaryString(src_opeval);
			str = str.substring(32-qtr);
			return str;
		}
	}
	
	public static void assemble(String objectProgramFile)
	{
		//TODO your assembler code
		//1. open the objectProgramFile in binary mode
		try {
			
			BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream(objectProgramFile)); 
			int BUFFER_SIZE=4;
			//2. write the firstCodeAddress to the file
			byte[] addr = ByteBuffer.allocate(BUFFER_SIZE).putInt(ParsedProgram.firstCodeAddress).array();
			fout.write(addr);
			//3. write the data to the file
			for(int i:ParsedProgram.data)
			{
				byte[] data = ByteBuffer.allocate(BUFFER_SIZE).putInt(i).array();
				fout.write(data);
			}
			//4. assemble one instruction at a time, and write to the file
			for(Instruction ins:ParsedProgram.code)
			{
				String operation_code="";
				int identifier=0;
				switch(ins.getOperationType())
				{
					case add:
							operation_code=operation_code+"00000";
							identifier=0;
							break;
					case addi:
							operation_code=operation_code+"00001";
							identifier=1;
							break;
					case sub:
							operation_code=operation_code+"00010";
							identifier=2;
							break;
					case subi:
							operation_code=operation_code+"00011";
							identifier=3;
							break;
					case mul:
							operation_code=operation_code+"00100";
							identifier=4;
							break;
					case muli:
							operation_code=operation_code+"00101";
							identifier=5;
							break;
					case div:
							operation_code=operation_code+"00110";
							identifier=6;
							break;
					case divi:
							operation_code=operation_code+"00111";
							identifier=7;
							break;
					case and:
							operation_code=operation_code+"01000";
							identifier=8;
							break;
					case andi:
							operation_code=operation_code+"01001";
							identifier=9;
							break;
					case or:
							operation_code=operation_code+"01010";
							identifier=10;
							break;
					case ori:
							operation_code=operation_code+"01011";
							identifier=11;
							break;
					case xor:
							operation_code=operation_code+"01100";
							identifier=12;
							break;
					case xori:
							operation_code=operation_code+"01101";
							identifier=13;
							break;
					case slt:
							operation_code=operation_code+"01110";
							identifier=14;
							break;
					case slti:
							operation_code=operation_code+"01111";
							identifier=15;
							break;
					case sll:
							operation_code=operation_code+"10000";
							identifier=16;
							break;
					case slli:
							operation_code=operation_code+"10001";
							identifier=17;
							break;
					case srl:
							operation_code=operation_code+"10010";
							identifier=18;
							break;
					case srli:
							operation_code=operation_code+"10011";
							identifier=19;
							break;
					case sra:
							operation_code=operation_code+"10100";
							identifier=20;
							break;
					case srai:
							operation_code=operation_code+"10101";
							identifier=21;
							break;
					case load:
							operation_code=operation_code+"10110";
							identifier=22;
							break;
					case store:
							operation_code=operation_code+"10111";
							identifier=23;
							break;
					case jmp:
							operation_code=operation_code+"11000";
							identifier=24;
							break;
					case beq:
							operation_code=operation_code+"11001";
							identifier=25;
							break;
					case bne:
							operation_code=operation_code+"11010";
							identifier=26;
							break;
					case blt:
							operation_code=operation_code+"11011";
							identifier=27;
							break;
					case bgt:
							operation_code=operation_code+"11100";
							identifier=28;
							break;
					case end:
							operation_code=operation_code+"11101";
							identifier=29;
							break;
					default:
							Misc.printErrorAndExit("unknown instruction!!");
				}
				int program_counter=ins.getProgramCounter();
				if(identifier==29)
				{
					operation_code=extra_zeroes(operation_code,27);
				}
				else if(identifier==24)
				{
					operation_code=extra_zeroes(operation_code,5);
					String ptr1=ins.getDestinationOperand().getLabelValue();
					int value_a = ParsedProgram.symtab.get(ptr1) - program_counter;
					operation_code+=str_converter(value_a,22);

				}
				else if(identifier>=25 && identifier<=28)
				{
					operation_code += str_converter(ins.getSourceOperand1().getValue(),5);
					operation_code += str_converter(ins.getSourceOperand2().getValue(),5);
					String ptr1=ins.getDestinationOperand().getLabelValue();
					int value_a = ParsedProgram.symtab.get(ptr1) - program_counter;
					operation_code+=str_converter(value_a,17);
				}
				else if(identifier<=20 && identifier%2==0)
				{
					operation_code += str_converter(ins.getSourceOperand1().getValue(),5);
					operation_code += str_converter(ins.getSourceOperand2().getValue(),5);
					operation_code += str_converter(ins.getDestinationOperand().getValue(),5);
					operation_code=extra_zeroes(operation_code,12);
				}
				else	
				{
					operation_code+=str_converter(ins.getSourceOperand1().getValue(),5);
					operation_code+=str_converter(ins.getDestinationOperand().getValue(),5);
					operation_code+=str_converter(ins.getSourceOperand2().getValue(),17);
				}
				int n=(int)Long.parseLong(operation_code, 2);
	            		byte[] instruction = ByteBuffer.allocate(4).putInt(n).array();
				fout.write(instruction);

			}
			//5. close the file
			fout.close();
        }
		catch (IOException ex) {
            ex.printStackTrace();
        }
		
		
		
		
	}
	
}
